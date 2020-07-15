package com.credenceid.sample.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.credenceid.database.BiometricDatabase.Status
import com.credenceid.database.FaceRecord
import com.credenceid.database.FingerprintRecord
import com.credenceid.database.FingerprintRecord.Position
import com.util.BitmapUtils
import kotlinx.android.synthetic.main.act_main.*
import java.io.IOException

private const val TAG = "DatabaseActivity"

private const val USER_ONE = 0
private const val USER_TWO = 1

class DatabaseActivity : AppCompatActivity() {

    private lateinit var faceRecordOne: FaceRecord
    private lateinit var faceRecordTwo: FaceRecord
    private var enrollFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()
    private var compareFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()
    private var faceRecords: Array<FaceRecord?> = arrayOf()
    private var lastEnrolledID = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        this.initValues()
        this.loadEnrollRecordS()
        this.loadEnrollFaceRecords()
        this.loadCompareRecords()
        this.configureLayoutComponents()
        //this.enrollAllUsersFP()
    }

    private fun initValues() {

        // This app will enroll two user records.
        enrollFPRecords += Array<FingerprintRecord?>(10) { null }
        enrollFPRecords += Array<FingerprintRecord?>(10) { null }
        // This app will be comparing enrolled records against two user records.
        compareFPRecords += Array<FingerprintRecord?>(10) { null }
        compareFPRecords += Array<FingerprintRecord?>(10) { null }

        //enrollAllFPRecords += Array<FingerprintRecord?>(100) { null }

        faceRecords = Array<FaceRecord?>(7){null}

        logBox.movementMethod = ScrollingMovementMethod()
    }

    private fun loadEnrollFaceRecords(){

        /*for (index in 1..faceRecords.size) {
            faceRecords[index-1] = FaceRecord(
                this.getBitmapFromAsset(
                    this,
                    "faceImages/face_record_$index.jpg"
                )
            )
        }*/
        faceRecordOne = FaceRecord(this.getBitmapFromAsset(
            this,
            "faceImages/face_record_1.jpg"
        ))
        faceRecordTwo = FaceRecord(this.getBitmapFromAsset(
            this,
            "faceImages/face_record_1.jpg"
        ))
    }

    private fun loadEnrollRecordS() {

        enrollFPRecords[USER_ONE][0] = FingerprintRecord(
            Position.RIGHT_THUMB,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RT.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][1] = FingerprintRecord(
            Position.RIGHT_INDEX,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RI.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][2] = FingerprintRecord(
            Position.RIGHT_MIDDLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RM.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][3] = FingerprintRecord(
            Position.RIGHT_RING,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RR.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][4] = FingerprintRecord(
            Position.RIGHT_LITTLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RL.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][5] = FingerprintRecord(
            Position.LEFT_THUMB,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LT.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][6] = FingerprintRecord(
            Position.LEFT_INDEX,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LI.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][7] = FingerprintRecord(
            Position.LEFT_RING,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LM.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][8] = FingerprintRecord(
            Position.LEFT_MIDDLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LR.wsq.bmp")
        )
        enrollFPRecords[USER_ONE][9] = FingerprintRecord(
            Position.LEFT_LITTLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LL.wsq.bmp")
        )

        enrollFPRecords[USER_TWO][0] = FingerprintRecord(
            Position.RIGHT_THUMB,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RT.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][1] = FingerprintRecord(
            Position.RIGHT_INDEX,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RI.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][2] = FingerprintRecord(
            Position.RIGHT_MIDDLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RM.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][3] = FingerprintRecord(
            Position.RIGHT_RING,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RR.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][4] = FingerprintRecord(
            Position.RIGHT_LITTLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_RL.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][5] = FingerprintRecord(
            Position.LEFT_THUMB,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LT.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][6] = FingerprintRecord(
            Position.LEFT_INDEX,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LI.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][7] = FingerprintRecord(
            Position.LEFT_RING,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LM.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][8] = FingerprintRecord(
            Position.LEFT_MIDDLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LR.wsq.bmp")
        )
        enrollFPRecords[USER_TWO][9] = FingerprintRecord(
            Position.LEFT_LITTLE,
            this.getBitmapFromAsset(this, "enrollImages/1000000000000001_LL.wsq.bmp")
        )
    }

    private fun loadCompareRecords() {

        compareFPRecords[USER_ONE][0] = FingerprintRecord(
            Position.RIGHT_THUMB,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RT.wsq.bmp")
        )
        compareFPRecords[USER_ONE][1] = FingerprintRecord(
            Position.RIGHT_INDEX,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RI.wsq.bmp")
        )
        compareFPRecords[USER_ONE][2] = FingerprintRecord(
            Position.RIGHT_MIDDLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RM.wsq.bmp")
        )
        compareFPRecords[USER_ONE][3] = FingerprintRecord(
            Position.RIGHT_RING,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RR.wsq.bmp")
        )
        compareFPRecords[USER_ONE][4] = FingerprintRecord(
            Position.RIGHT_LITTLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RL.wsq.bmp")
        )
        compareFPRecords[USER_ONE][5] = FingerprintRecord(
            Position.LEFT_THUMB,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LT.wsq.bmp")
        )
        compareFPRecords[USER_ONE][6] = FingerprintRecord(
            Position.LEFT_INDEX,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LI.wsq.bmp")
        )
        compareFPRecords[USER_ONE][7] = FingerprintRecord(
            Position.LEFT_RING,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LM.wsq.bmp")
        )
        compareFPRecords[USER_ONE][8] = FingerprintRecord(
            Position.LEFT_MIDDLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LR.wsq.bmp")
        )
        compareFPRecords[USER_ONE][9] = FingerprintRecord(
            Position.LEFT_LITTLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LL.wsq.bmp")
        )

        compareFPRecords[USER_TWO][0] = FingerprintRecord(
            Position.RIGHT_THUMB,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RT.wsq.bmp")
        )
        compareFPRecords[USER_TWO][1] = FingerprintRecord(
            Position.RIGHT_INDEX,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RI.wsq.bmp")
        )
        compareFPRecords[USER_TWO][2] = FingerprintRecord(
            Position.RIGHT_MIDDLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RM.wsq.bmp")
        )
        compareFPRecords[USER_TWO][3] = FingerprintRecord(
            Position.RIGHT_RING,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RR.wsq.bmp")
        )
        compareFPRecords[USER_TWO][4] = FingerprintRecord(
            Position.RIGHT_LITTLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_RL.wsq.bmp")
        )
        compareFPRecords[USER_TWO][5] = FingerprintRecord(
            Position.LEFT_THUMB,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LT.wsq.bmp")
        )
        compareFPRecords[USER_TWO][6] = FingerprintRecord(
            Position.LEFT_INDEX,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LI.wsq.bmp")
        )
        compareFPRecords[USER_TWO][7] = FingerprintRecord(
            Position.LEFT_RING,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LM.wsq.bmp")
        )
        compareFPRecords[USER_TWO][8] = FingerprintRecord(
            Position.LEFT_MIDDLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LR.wsq.bmp")
        )
        compareFPRecords[USER_TWO][9] = FingerprintRecord(
            Position.LEFT_LITTLE,
            this.getBitmapFromAsset(this, "compareImages/2000000000000001_LL.wsq.bmp")
        )
    }

    private fun configureLayoutComponents() {

        enrollUserOneBtn.setOnClickListener {
            log("Enrolling user ONE. : $faceRecordOne")
            App.BioManager!!.enroll(10, enrollFPRecords[USER_ONE], faceRecordOne, null) { status, id ->
                log("[Status: $status, ID: $id]")
                if (Status.SUCCESS == status) lastEnrolledID = id
            }
        }
        enrollUserTwoBtn.setOnClickListener {
            log("Enrolling user TWO. : $faceRecordTwo")
            App.BioManager!!.enroll(20, enrollFPRecords[USER_TWO], faceRecordTwo, null) { status, id ->
                log("[Status: $status, ID: $id]")
                if (Status.SUCCESS == status) lastEnrolledID = id
            }

        }

        enrollAllBtn.setOnClickListener {
            log("Enrolling all users.")
            for (index in 1..50) {
                App.BioManager!!.enroll(index, enrollAllFPRecords[index-1], null, null) { status, id ->
                    log("[Status: $status, ID: $id]")
                    if (Status.SUCCESS == status) lastEnrolledID = id
                }
            }
        }

        readBtn.setOnClickListener {
            log("Reading user with ID $lastEnrolledID")
            App.BioManager!!.read(lastEnrolledID) { status, fpRecords, faceRecord, irisRecords ->
                var fpLen = 0; for (fp in fpRecords) if (null != fp) ++fpLen
                var irisLen = 0; for (iris in irisRecords) if (null != iris) ++irisLen
                val hasFace = (null != faceRecord)
                log("[Status: $status, FP Count: $fpLen, Face: $hasFace, Iris Count: $irisLen]")
            }
        }

        matchUserBtn.setOnClickListener {
            log("Matching USER_ONE against DB.")
            App.BioManager!!.match(compareFPRecords[USER_TWO], faceRecordTwo, null) { status, arrayList ->
                log("[Status: $status, Match Count: ${arrayList?.size}]")

                if (null == arrayList) return@match
                for (item in arrayList) {
                    log("[FP: ${item.fingerprintScore}," +
                            "Face: ${item.faceScore}, Iris: ${item.irisScore}]")
                }
            }
        }

        verifyBtn.setOnClickListener {
            log("Verifying USER_ONE against user with ID 1")
            App.BioManager!!.verify(10, compareFPRecords[USER_ONE], faceRecordOne, null)
            { status, matchItem ->
                log("[Status: $status, FP: ${matchItem.fingerprintScore}," +
                        "Face: ${matchItem.faceScore}, Iris: ${matchItem.irisScore}]")
            }
        }

        deleteBtn.setOnClickListener {
            log("Deleting user with ID $lastEnrolledID")
            App.BioManager!!.delete(lastEnrolledID) { status -> log("[Status: $status]") }
        }
    }

    private fun log(msg: String) = logBox.append("==> $msg\n")

    private fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {

        return try {
            BitmapFactory.decodeStream(context.assets.open(filePath!!))
        } catch (ignore: IOException) {
            null
        }
    }

    private var enrollAllFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()
    private val fingerArray = arrayOf("RT","RI","RM","RR","RL","LT", "LI","LM","LR","LL")

    private fun enrollAllUsersFP(){
        for (userIndex in 1..50){
            enrollAllFPRecords += Array<FingerprintRecord?>(10){null}
            val userIndexVal = if(userIndex < 10) "0${userIndex}" else "$userIndex"
            for (fingerIndex in 0..9){
                val filePath = "enrollAllImages/10000000000000${userIndexVal}_${fingerArray[fingerIndex]}.wsq.bmp"
                Log.d(TAG, "File Path :$filePath")
                Log.d(TAG, "Userindex :${userIndex-1} and fingerIndex :$fingerIndex");
                enrollAllFPRecords[userIndex-1][fingerIndex] = FingerprintRecord(
                    Position.valueOf(fingerIndex),
                    this.getBitmapFromAsset(this, filePath)
                )
            }
        }
    }
}