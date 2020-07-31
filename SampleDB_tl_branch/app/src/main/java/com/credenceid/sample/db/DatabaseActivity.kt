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
import kotlinx.android.synthetic.main.act_main.*
import java.io.File
import java.io.IOException
import java.util.logging.Logger
import java.util.Calendar
private const val TAG = "DatabaseActivity"

private val READFROMSDCARD = true
private var SDCARD_PATH : String = ""



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
            enrollOneBtn.visibility = View.GONE
            enrollAllBtn.visibility = View.GONE
            deleteBtn.visibility = View.GONE
            matchUserBtn.visibility = View.GONE
            this.numberUser = 0
        }
    }

    private fun prepareListFromAsset(n : Int) {

        startTimer()
        var folders = assets.list("EnrollBMPImages")?.toMutableList()
        //folders?.shuffle()
        this.randomEnrollList = folders?.take(n)!!.sorted()
        this.compareList = assets.list("CompareBMPImages")?.toList()!!.sorted()
        log("Loading list: ${endTimer()}s")
    }

    private fun prepareListFromSdcard(n : Int) {

        startTimer()
        var file = File(getStoragePath() + "EnrollBMPImages")
        var folders = file.list().toList().map { it.toInt() }.sorted().map { it.toString() }
        this.randomEnrollList = folders.take(n)
        this.compareList = File(getStoragePath() + "CompareBMPImages").list().toList()
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
                val fpPath = "CompareBMPImages/${id}/${key}"
                var fpBmp = if (READFROMSDCARD) this.getBitmapFromSdcard(this, fpPath) else this.getBitmapFromAsset(this,fpPath)
                compareFPRecord[i] = FingerprintRecord(
                    fp,
                    fpBmp
                )
                i += 1
            }
            compareFPRecords += compareFPRecord
            var facePath = "CompareBMPImages/${id}/face.jpg"
            var faceBmp = if (READFROMSDCARD) this.getBitmapFromSdcard(this, facePath) else this.getBitmapFromAsset(this, facePath)
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
                var fpPath = "EnrollBMPImages/${id}/${key}"
                var fpBmp = if (READFROMSDCARD) this.getBitmapFromSdcard(this, fpPath) else this.getBitmapFromAsset(this, fpPath)
                enrollFPRecord[i] = FingerprintRecord(
                    fp,
                    fpBmp
                )
                i += 1
            }
            enrollFPRecords += enrollFPRecord
            var facePath = "EnrollBMPImages/${id}/face.jpg"
            var faceBmp = if (READFROMSDCARD) this.getBitmapFromSdcard(this, facePath) else this.getBitmapFromAsset(this, facePath)
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
            if (READFROMSDCARD){this.prepareListFromSdcard(n) } else{ this.prepareListFromAsset(n) }
            Logger.getLogger(DatabaseActivity::class.java.name).info(this.randomEnrollList.toString())
            Logger.getLogger(DatabaseActivity::class.java.name).info(this.compareList.toString())

//            log("Preparing $n users")
//            this.loadEnrollRecords()
//            this.loadCompareRecords()
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

//            var n = randomEnrollList.size
//            var eThread = enrollThread(n, logBox, randomEnrollList, enrollFPRecords,enrollFaceRecords)
//            eThread.start()

            var eThread = enrollThread2(this, logBox, randomEnrollList)
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

    private fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
        return try {
            BitmapFactory.decodeStream(context.assets.open(filePath!!))
        } catch (ignore: IOException) {
            Logger.getLogger(DatabaseActivity::class.java.name).info("FP not found")
            null
        }
    }

    private fun getBitmapFromSdcard(context: Context, filePath: String?): Bitmap? {
        var path = getStoragePath() + filePath!!
        return try {
//            var iS = FileInputStream(File(getStoragePath() + filePath!!))
//            BitmapFactory.decodeStream(iS)
            BitmapFactory.decodeFile(path)
        } catch (ignore: IOException) {
            Logger.getLogger(DatabaseActivity::class.java.name).info("$path :path not found")
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

    class enrollThread2(context: Context, logBox: TextView,randomEnrollList: List<String>): Thread(){
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

        var context = context
        var logBox = logBox
        var randomEnrollList = randomEnrollList
        fun log(msg: String) = logBox.append("==> $msg\n")

        override fun run(){
            startTimer()
            counter = eachCounter
            while (eachCounter != randomEnrollList.size) {
                if (doneEnroll){
                    doneEnroll = false
                    //init data
                    var id = randomEnrollList[eachCounter].toInt()
                    var enrollFPRecord = Array<FingerprintRecord?>(10) { null }
                    var facePath = "EnrollBMPImages/${id}/face.jpg"
                    var i = 0

                    //Create FP record
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$id record is being created")
                    for ((key,fp) in fpMapping){
                        val fpPath = "EnrollBMPImages/${id}/${key}"
                        val fpBmp = this.getBitmapFromSrc(context, fpPath)
                        enrollFPRecord[i] = FingerprintRecord(fp, fpBmp)
                        i += 1
                    }

                    //Create Face record
                    var enrollFaceRecord = FaceRecord(getBitmapFromSrc(context, facePath))

                    //Enroll record to biomanager
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$id is being enrolled")
                    App.BioManager!!.enroll(
                        id,
                        enrollFPRecord,
                        enrollFaceRecord,
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
        private fun getBitmapFromSrc(context: Context, filePath : String) : Bitmap?{
            try {
                if (READFROMSDCARD){
                    return BitmapFactory.decodeFile(SDCARD_PATH + filePath!!)
                }else{
                    return BitmapFactory.decodeStream(context.assets.open(filePath!!))
                }
            } catch (ignore: IOException) {
                Logger.getLogger(DatabaseActivity::class.java.name).info("FP not found")
                return null
            }

        }

    }
}

