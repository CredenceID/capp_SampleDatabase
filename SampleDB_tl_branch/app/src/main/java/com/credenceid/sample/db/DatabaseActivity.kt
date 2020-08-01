package com.credenceid.sample.db

import android.content.Context

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.credenceid.database.FaceRecord
import com.credenceid.database.FingerprintRecord
import com.credenceid.database.FingerprintRecord.Position
import com.opencsv.CSVWriter
import kotlinx.android.synthetic.main.act_main.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import java.util.Calendar
private const val TAG = "DatabaseActivity"

private val READFROMSDCARD = true
private var SDCARD_PATH : String = ""



class DatabaseActivity : AppCompatActivity() {
    private var randomEnrollList: List<String> = listOf()
    private var compareList : List<String> = listOf()
    private var usersEditText : EditText? = null
    private var numberUser : Int? = null



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        usersEditText = findViewById<EditText>(R.id.users_edittext)
        SDCARD_PATH = "${android.os.Environment.getExternalStorageDirectory()}/";
        this.getTotalUser()
        this.configureLayoutComponents()

    }

    private fun getTotalUser() {
        try {
            if (READFROMSDCARD) {
                this.numberUser = File(getStoragePath() + "EnrollBMPImages").list().size
                var c = File(getStoragePath() + "CompareBMPImages").list().size
            } else {
                this.numberUser = assets.list("EnrollBMPImages")?.size
                var c = assets.list("CompareBMPImages")?.size
            }
            log("Data Found!")
        } catch (ignore: Exception){
            log("Data not found!")
            prepareBtn.visibility = View.GONE
            enrollAllBtn.visibility = View.GONE
            deleteBtn.visibility = View.GONE
            matchUserBtn.visibility = View.GONE
            this.numberUser = 0
        }
    }

    private fun prepareList(n : Int){
        startTimer()
        if (READFROMSDCARD){
            var file = File(getStoragePath() + "EnrollBMPImages")
            var folders = file.list().toList().map { it.toInt() }.sorted().map { it.toString() }
            this.randomEnrollList = folders.take(n)
            this.compareList = File(getStoragePath() + "CompareBMPImages").list().toList()
        }else{
            var folders = assets.list("EnrollBMPImages")!!.toList().map { it.toInt() }.sorted().map { it.toString() }
            //folders?.shuffle()
            this.randomEnrollList = folders.take(n)
            this.compareList = assets.list("CompareBMPImages")?.toList()!!
        }
        log("Loading list: ${endTimer()}s")

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
            this.prepareList(n)
            Logger.getLogger(DatabaseActivity::class.java.name).info(this.randomEnrollList.toString())
            Logger.getLogger(DatabaseActivity::class.java.name).info(this.compareList.toString())
        }


        enrollAllBtn.setOnClickListener {
            var eThread = enrollThread(this, logBox, randomEnrollList)
            eThread.start()
        }

//        matchUserBtn.setOnClickListener {
//            log("Matching all ${compareList.size} compareList against DB.")
//            startTimer()
//            counter = 0
//
//            for ((i,compareFPRecord) in compareFPRecords.withIndex()) {
//                App.BioManager!!.match(
//                    compareFPRecord,
//                    compareFaceRecords[i],
//                    null
//                ) { status, arrayList ->
//                    log("[Status: $status, Match Count: ${arrayList?.size}]")
//
//                    if (null == arrayList) return@match
//                    for (item in arrayList) {
//                        log(
//                            "[FP: ${item.fingerprintScore}," +
//                                    "Face: ${item.faceScore}, Iris: ${item.irisScore}]"
//                        )
//                    }
//                    counter = counter!!.inc()
//                    if (counter == compareFPRecords.size){
//                        log("Enrolling to BioManager: ${endTimer()}s")
//                    }
//                }
//            }
//        }

        deleteBtn.setOnClickListener {
            log("Deleting all enrolled user")
            for (id in compareList) {
                App.BioManager!!.delete(id.toInt()){status -> log("[Status: $status]") }
            }
        }

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

    private fun getStoragePath() = SDCARD_PATH

    fun log(msg: String) = logBox.append("==> $msg\n")

    companion object{
        var startTime : Long? = null
        var endTime : Long? = null
        fun startTimer(){
            startTime = Calendar.getInstance().timeInMillis
        }
        fun endTimer(): Int {
            endTime = Calendar.getInstance().timeInMillis
            return ((endTime!! - startTime!!)/1000).toInt()
        }

        fun openCSV(fileName : String): BufferedWriter {
            val filePath = SDCARD_PATH + fileName
            val file = File(filePath)
            if (file.exists()){
                file.delete()
            }
            file.createNewFile()
            Logger.getLogger(DatabaseActivity::class.java.name).info("Create file at: ${filePath}")


            var fw = FileWriter(file.absoluteFile)
            var bw = BufferedWriter(fw)
            bw!!.write("\"ID\", Duration \n")
            return bw
        }
        fun writeCSV(bw : BufferedWriter, id : Int, duration : Int){
            bw.write("${id}, ${duration} \n")
            bw.flush()
        }
        fun closeCSV(bw : BufferedWriter){
            Logger.getLogger(DatabaseActivity::class.java.name).info("Close file")
            Thread.sleep(1000)
            bw.close()
        }
    }

    class enrollThread(context: Context, logBox: TextView, randomEnrollList: List<String>): Thread(){
        var fpMapping = mutableMapOf(
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
        var context = context
        var logBox = logBox //unsafe reference to view
        var randomEnrollList = randomEnrollList
        fun log(msg: String) = logBox.append("==> $msg\n")
        var thread_counter = 0
        var doneEnroll: Boolean = true

        var bw :BufferedWriter? = null

        override fun run(){
            bw = openCSV("Enroll.csv")
            while (thread_counter != randomEnrollList.size) {
                if (doneEnroll){
                    doneEnroll = false
                    //init data
                    var iD = randomEnrollList[thread_counter].toInt()
                    var enrollFPRecord = Array<FingerprintRecord?>(10) { null }
                    var facePath = "EnrollBMPImages/${iD}/face.jpg"
                    var i = 0

                    //Create FP record
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$iD record is being created")
                    for ((key,fp) in fpMapping){
                        val fpPath = "EnrollBMPImages/${iD}/${key}"
                        val fpBmp = this.getBitmapFromSrc(context, fpPath)
                        enrollFPRecord[i] = FingerprintRecord(fp, fpBmp)
                        i += 1
                    }

                    //Create Face record
                    var enrollFaceRecord = FaceRecord(getBitmapFromSrc(context, facePath))

                    //Enroll record to biomanager
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$iD is being enrolled")
                    startTimer()
                    App.BioManager!!.enroll(
                        iD,
                        enrollFPRecord,
                        enrollFaceRecord,
                        null
                    ) { status, id ->
                        log("[Status: $status, ID: $id]")
                        thread_counter = thread_counter.inc()
                        doneEnroll = true
                        val duration = endTimer()
                        log("Enrolled to BioManager: ${duration}s")
                        writeCSV(bw!!, id, duration)
                    }
                }
            }
            closeCSV(bw!!)
        }

        private fun getBitmapFromSrc(context: Context, filePath : String) : Bitmap?{
            try {
                if (READFROMSDCARD){
                    return BitmapFactory.decodeFile(SDCARD_PATH + filePath)
                }else{
                    return BitmapFactory.decodeStream(context.assets.open(filePath))
                }
            } catch (ignore: IOException) {
                Logger.getLogger(DatabaseActivity::class.java.name).info("FP not found")
                return null
            }
        }
    }
}

