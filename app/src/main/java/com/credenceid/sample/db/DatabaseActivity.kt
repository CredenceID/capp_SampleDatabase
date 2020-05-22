package com.credenceid.sample.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.credenceid.database.BiometricDatabase.Status
import com.credenceid.database.FingerprintRecord
import com.credenceid.database.FingerprintRecord.Position
import kotlinx.android.synthetic.main.act_main.*
import java.io.IOException

private const val TAG = "DatabaseActivity"

private const val USER_ONE = 0
private const val USER_TWO = 1

class DatabaseActivity : AppCompatActivity() {

    private var enrollFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()
    private var compareFPRecords: Array<Array<FingerprintRecord?>> = arrayOf()
    private var lastEnrolledID = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        this.initValues()
        this.loadEnrollRecordS()
        this.loadCompareRecords()
        this.configureLayoutComponents()
    }

    private fun initValues() {

        // This app will enroll two user records.
        enrollFPRecords += Array<FingerprintRecord?>(10) { null }
        enrollFPRecords += Array<FingerprintRecord?>(10) { null }
        // This app will be comparing enrolled records against two user records.
        compareFPRecords += Array<FingerprintRecord?>(10) { null }
        compareFPRecords += Array<FingerprintRecord?>(10) { null }

        logBox.movementMethod = ScrollingMovementMethod()
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
            log("Enrolling user ONE.")
            App.BioManager!!.enroll(10, enrollFPRecords[USER_ONE], null, null) { status, id ->
                log("[Status: $status, ID: $id]")
                if (Status.SUCCESS == status) lastEnrolledID = id
            }
        }
        enrollUserTwoBtn.setOnClickListener {
            log("Enrolling user TWO.")
            App.BioManager!!.enroll(20, enrollFPRecords[USER_TWO], null, null) { status, id ->
                log("[Status: $status, ID: $id]")
                if (Status.SUCCESS == status) lastEnrolledID = id
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
            App.BioManager!!.match(compareFPRecords[USER_ONE], null, null) { status, arrayList ->
                log("[Status: $status, Match Count: ${arrayList?.size}]")

                if (null == arrayList) return@match
                for (item in arrayList) {
                    log("[FP: ${item.fingerprintScore}," +
                            "Face: ${item.faceScore}, Iris: ${item.irisScore}]")
                }
            }
        }

        verifyBtn.setOnClickListener {
            log("Verifying USER_ONE against user with ID $lastEnrolledID")
            App.BioManager!!.verify(lastEnrolledID, compareFPRecords[USER_ONE], null, null)
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
}