package com.credenceid.sample.db

import android.content.Context

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.credenceid.database.FaceRecord
import com.credenceid.database.FingerprintRecord
import com.credenceid.database.FingerprintRecord.Position
import kotlinx.android.synthetic.main.act_main.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.logging.Logger
import java.util.Calendar
private const val TAG = "DatabaseActivity"

private const val USER_ONE = 0
private const val USER_TWO = 1

class DatabaseActivity : AppCompatActivity() {

    private var enrollFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()
    private var compareFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()

    private var enrollFaceRecords: Array<FaceRecord?> = arrayOf()
    private var compareFaceRecords: Array<FaceRecord?> = arrayOf()

    private var fpMapping = mutableMapOf(
        "LI" to Position.LEFT_INDEX,
        "LL" to Position.LEFT_LITTLE,
        "LM" to Position.LEFT_MIDDLE,
        "LR" to Position.LEFT_RING,
        "LT" to Position.LEFT_THUMB,
        "RI" to Position.RIGHT_INDEX,
        "RL" to Position.RIGHT_LITTLE,
        "RM" to Position.RIGHT_MIDDLE,
        "RR" to Position.RIGHT_RING,
        "RT" to Position.RIGHT_THUMB)

    private var randomEnrollList: List<String> = listOf()
    private var compareList : List<String> = listOf()
    private var usersEditText : EditText? = null
    private var numberUser : Int? = null

    private val SDCARD_PATH : String = "${android.os.Environment.getExternalStorageDirectory()}/";

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        usersEditText = findViewById<EditText>(R.id.users_edittext)

        this.getTotalUser()
        this.configureLayoutComponents()

    }

    private fun getTotalUser() {this.numberUser = assets.list("EnrollBMPImages")?.size}

    private fun load_list(n : Int) {

        startTimer()
        var folders = assets.list("EnrollBMPImages")?.toMutableList()
        //folders?.shuffle()
        this.randomEnrollList = folders?.take(n)!!
        this.compareList = assets.list("CompareBMPImages")?.toList()!!
        log("Loading list: ${endTimer()}s")
    }

    private fun loadCompareRecords() {
        compareFPRecords = arrayOf()
        compareFaceRecords = arrayOf()
        startTimer()
        for (id in this.compareList){
            var compareFPRecord = Array<FingerprintRecord?>(10) { null }
            var i = 0
            for ((key,fp) in fpMapping){
                compareFPRecord[i] = FingerprintRecord(
                    fp,
                    this.getBitmapFromAsset(this, "CompareBMPImages/${id}/${key}")
                )
                i += 1
            }
            compareFPRecords += compareFPRecord

            var faceBmp = this.getBitmapFromAsset(this, "CompareBMPImages/${id}/face.jpg")
            compareFaceRecords += FaceRecord(faceBmp)
        }
        val duration = endTimer()
        log("Loaded CompareRecords: ${duration}s")
    }

    private fun loadEnrollRecords() {
        enrollFPRecords = arrayOf()
        enrollFaceRecords = arrayOf()
        startTimer()
        for (id in this.randomEnrollList){
            var enrollFPRecord = Array<FingerprintRecord?>(10) { null }
            var i = 0
            for ((key,fp) in fpMapping){
                enrollFPRecord[i] = FingerprintRecord(
                    fp,
                    this.getBitmapFromAsset(this, "EnrollBMPImages/${id}/${key}")
                )
                i += 1
            }
            enrollFPRecords += enrollFPRecord

            var faceBmp = this.getBitmapFromAsset(this, "EnrollBMPImages/${id}/face.jpg")
            enrollFaceRecords += FaceRecord(faceBmp)
        }
        log("Loaded EnrollRecords: ${endTimer()}s")
    }

    private fun configureLayoutComponents() {
        users_edittext.setHint("1-${this.numberUser}")

        prepareBtn.setOnClickListener {

            var n: Int = 0
            if ((usersEditText!!.text.isNullOrBlank()) || (usersEditText!!.text.toString().toInt() > numberUser!!)){
                return@setOnClickListener
            } else{
                n = usersEditText!!.text.toString().toInt()
            }

            this.load_list(n)
            startTimer()
            log("Preparing $n users")
            this.loadEnrollRecords()
            this.loadCompareRecords()
            log("Prepared data: ${endTimer()}s")
            eachCounter = 0
        }

        enrollOneBtn.setOnClickListener {
            startTimer()
            if (eachCounter != randomEnrollList.size) {
                App.BioManager!!.enroll(
                    randomEnrollList[eachCounter].toInt(),
                    enrollFPRecords[eachCounter],
                    enrollFaceRecords[eachCounter],
                    null
                ) { status, id ->
                    log("[Status: $status, ID: $id]")
                    log("Enrolled ${id} to BioManager: ${endTimer()}")
                }
                eachCounter += 1
            }
        }

        enrollAllBtn.setOnClickListener {

            var n = randomEnrollList.size
            var eThread = enrollThread(n, logBox, randomEnrollList, enrollFPRecords,enrollFaceRecords)
            eThread.start()
//            var n = randomEnrollList.size
//            startTimer()
//            log("Enrolling $n users")
//            counter = eachCounter
//            doneEnroll = true
//            while (eachCounter != randomEnrollList.size){
//                var id = randomEnrollList[eachCounter].toInt()
//                Logger.getLogger(DatabaseActivity::class.java.name).info("$id is being enrolled")
//                App.BioManager!!.enroll(id, enrollFPRecords[eachCounter], enrollFaceRecords[eachCounter], null) { status, id ->
//                    log("[Status: $status, ID: $id]")
//                    counter = counter!!.inc()
//                    doneEnroll = true
//                    if (counter == randomEnrollList.size) {
//                        log("Enrolled to BioManager: ${endTimer()}s")
//                    }
//                }
//                Thread.sleep(10_000)
//                eachCounter += 1
//            }

        }

        matchUserBtn.setOnClickListener {
            log("Matching all ${compareList.size} compareList against DB.")
            startTimer()
            counter = 0

            for ((i,compareFPRecord) in compareFPRecords.withIndex()) {
                App.BioManager!!.match(
                    compareFPRecord,
                    compareFaceRecords[i],
                    null
                ) { status, arrayList ->
                    log("[Status: $status, Match Count: ${arrayList?.size}]")

                    if (null == arrayList) return@match
                    for (item in arrayList) {
                        log(
                            "[FP: ${item.fingerprintScore}," +
                                    "Face: ${item.faceScore}, Iris: ${item.irisScore}]"
                        )
                    }
                    counter = counter!!.inc()
                    if (counter == compareFPRecords.size){
                        log("Enrolling to BioManager: ${endTimer()}s")
                    }
                }
            }
        }

        deleteBtn.setOnClickListener {
            log("Deleting all enrolled user")
            for (id in compareList) {
                App.BioManager!!.delete(id.toInt()){status -> log("[Status: $status]") }
            }
        }

//
//        readBtn.setOnClickListener {
//            log("Reading user with ID $lastEnrolledID")
//            App.BioManager!!.read(lastEnrolledID) { status, fpRecords, faceRecord, irisRecords ->
//                var fpLen = 0; for (fp in fpRecords) if (null != fp) ++fpLen
//                var irisLen = 0; for (iris in irisRecords) if (null != iris) ++irisLen
//                val hasFace = (null != faceRecord)
//                log("[Status: $status, FP Count: $fpLen, Face: $hasFace, Iris Count: $irisLen]")
//            }
//        }
//

//
//        verifyBtn.setOnClickListener {
//            log("Verifying USER_ONE against user with ID $lastEnrolledID")
//            App.BioManager!!.verify(1, compareFPRecords[USER_ONE], null, null)
//            { status, matchItem ->
//                log("[Status: $status, FP: ${matchItem.fingerprintScore}," +
//                        "Face: ${matchItem.faceScore}, Iris: ${matchItem.irisScore}]")
//            }
//        }
//

    }

    private fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
        return try {
            BitmapFactory.decodeStream(context.assets.open(filePath!!))
        } catch (ignore: IOException) {
            Logger.getLogger(DatabaseActivity::class.java.name).info("FP not found")
            null
        }
    }

    private fun getStoragePath() = SDCARD_PATH

    fun log(msg: String) = logBox.append("==> $msg\n")


    companion object{
        var startTime : Long? = null
        var endTime : Long? = null
        var counter : Int? = null
        var doneEnroll: Boolean = true
        var eachCounter : Int = 0
        fun startTimer(){
            startTime = Calendar.getInstance().timeInMillis
        }
        fun endTimer(): Int {
            endTime = Calendar.getInstance().timeInMillis
            return ((endTime!! - startTime!!)/1000).toInt()
        }

    }

    class enrollThread(n: Int, logBox: TextView,randomEnrollList: List<String>, enrollFPRecords : Array<Array<FingerprintRecord?>>,enrollFaceRecords : Array<FaceRecord?>): Thread(){
        var n = n
        var logBox = logBox
        var randomEnrollList = randomEnrollList
        var enrollFaceRecords = enrollFaceRecords
        var enrollFPRecords = enrollFPRecords
        fun log(msg: String) = logBox.append("==> $msg\n")

        override fun run(){
            startTimer()
            counter = eachCounter
            while (eachCounter != randomEnrollList.size) {
                if (doneEnroll){
                    doneEnroll = false
                    var id = randomEnrollList[eachCounter].toInt()
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$id is being enrolled")
                    App.BioManager!!.enroll(
                        id,
                        enrollFPRecords[eachCounter],
                        enrollFaceRecords[eachCounter],
                        null
                    ) { status, id ->
                        log("[Status: $status, ID: $id]")
                        counter = counter!!.inc()
                        doneEnroll = true
                        if (counter == randomEnrollList.size) {
                            log("Enrolled to BioManager: ${endTimer()}s")
                        }
                    }
                    eachCounter += 1
                }
            }
        }

    }
}
