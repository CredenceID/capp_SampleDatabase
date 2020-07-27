package com.credenceid.sample.db

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.credenceid.database.FaceRecord
import com.credenceid.database.FingerprintRecord
import com.credenceid.database.FingerprintRecord.Position
import kotlinx.android.synthetic.main.act_main.*
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
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        this.load_list(2)
        this.loadEnrollRecords()
        this.loadCompareRecords()
        this.configureLayoutComponents()
    }

    private fun load_list(n : Int) {
        startTimer()
        var folders = assets.list("EnrollBMPImages")?.toMutableList()
        folders?.shuffle()
        this.randomEnrollList = folders?.take(n)!!
        this.compareList = assets.list("CompareBMPImages")?.toList()!!
        log("Loading list: ${endTimer()}s")
    }

    private fun loadCompareRecords() {
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
        enrollBtn.setOnClickListener {
            startTimer()
            val n = randomEnrollList.size
            log("Enrolling $n users")
            for ((i,iD) in randomEnrollList.withIndex()) {
                Logger.getLogger(DatabaseActivity::class.java.name).info("$i $iD enrolled to biometric ")
                App.BioManager!!.enroll(-1, enrollFPRecords[i], enrollFaceRecords[i], null) { status, id ->
                    log("[Status: $status, ID: $id]")
                    if (id == randomEnrollList[n-1].toInt()){
                        val duration = endTimer()
                        log("Enrolling to BioManager: ${duration}s")
                    }
                }
            }
        }

        matchUserBtn.setOnClickListener {
            log("Matching all ${compareList.size} compareList against DB.")

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

    private fun log(msg: String) = logBox.append("==> $msg\n")


    private fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
        return try {
            BitmapFactory.decodeStream(context.assets.open(filePath!!))
        } catch (ignore: IOException) {
            null
        }
    }

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
    }
}