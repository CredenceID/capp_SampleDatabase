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
import java.io.*
import java.util.logging.Logger
import java.util.Calendar
private const val TAG = "DatabaseActivity"

private val READFROMSDCARD = true
private var SDCARD_PATH : String = ""



class DatabaseActivity : AppCompatActivity() {
    private var enrollList: List<String> = listOf()
    private var compareList : List<String> = listOf()
    private var enrollEditText : EditText? = null
    private var compareEditText: EditText? = null
    private var numberCompareUser : Int? = 0
    private var numberEnrollUser : Int? = 0




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        enrollEditText = findViewById<EditText>(R.id.enrollEditText)
        compareEditText = findViewById<EditText>(R.id.compareEditText)
        SDCARD_PATH = "${android.os.Environment.getExternalStorageDirectory()}/";
        this.getTotalUser()
        this.configureLayoutComponents()

    }

    private fun getTotalUser() {
        try {
            if (READFROMSDCARD) {
                this.numberEnrollUser = File(getStoragePath() + "EnrollBMPImages").list().size
                this.numberCompareUser = File(getStoragePath() + "CompareBMPImages").list().size
            } else {
                this.numberEnrollUser = assets.list("EnrollBMPImages")?.size
                this.numberCompareUser = assets.list("CompareBMPImages")?.size
            }
            log("Data Found!")
        } catch (ignore: Exception){
            log("Data not found!")
            prepareBtn.visibility = View.GONE
            enrollAllBtn.visibility = View.GONE
            deleteBtn.visibility = View.GONE
            matchBtn.visibility = View.GONE
        }
    }

    private fun prepareList(enrolled : Int, compared : Int){
        startTimer()
        if (READFROMSDCARD){
            var enrollFileName = File(getStoragePath() + "EnrollBMPImages").list().toList().map { it.toInt() }.sorted().map { it.toString() }
            var compareFileName = File(getStoragePath() + "CompareBMPImages").list().toList().map { it.toInt() }.sorted().map { it.toString() }
            if (shuffle_switch.isChecked){
                enrollFileName = enrollFileName.shuffled()
                compareFileName = compareFileName.shuffled()
            }
            this.enrollList = enrollFileName.take(enrolled)
            this.compareList = compareFileName.take(compared)
        }else{
            //is not updated.. Don't use
            var folders = assets.list("EnrollBMPImages")!!.toList().map { it.toInt() }.sorted().map { it.toString() }.take(compared)
            //folders?.shuffle()
            this.enrollList = folders.take(enrolled)
            this.compareList = assets.list("CompareBMPImages")?.toList()!!.take(compared)
        }
        log("Loading list: ${endTimer()}s")

    }

    private fun configureLayoutComponents() {
        enrollEditText!!.setHint("1-${this.numberEnrollUser}")
        compareEditText!!.setHint("1-${this.numberCompareUser}")

        prepareBtn.setOnClickListener {
            var enrolled: Int
            var compared: Int

            if ((enrollEditText!!.text.isNullOrBlank()) || (enrollEditText!!.text.toString().toInt() > numberEnrollUser!!) || (compareEditText!!.text.isNullOrBlank()) || (compareEditText!!.text.toString().toInt() > numberCompareUser!!) ){
                log("Wrong Input")
                return@setOnClickListener
            } else{
                enrolled = enrollEditText!!.text.toString().toInt()
                compared = compareEditText!!.text.toString().toInt()
            }
            this.prepareList(enrolled, compared)
            Logger.getLogger(DatabaseActivity::class.java.name).info(this.enrollList.toString())
            Logger.getLogger(DatabaseActivity::class.java.name).info(this.compareList.toString())
        }


        enrollAllBtn.setOnClickListener {
            var eThread = enrollThread(this, logBox, enrollList, fp_switch.isChecked, face_switch.isChecked)
            eThread.start()
        }

        matchBtn.setOnClickListener {
            var mThread = matchThread(this, logBox, compareList, fp_switch.isChecked, face_switch.isChecked)
            mThread.start()
        }


        deleteBtn.setOnClickListener {
            log("Not function implemented")
        }
//            log("Deleting all enrolled user")
//            for (id in compareList) {
//                App.BioManager!!.delete(id.toInt()){status -> log("[Status: $status]") }
//            }
//        }

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

    class enrollThread(context: Context, logBox: TextView, randomEnrollList: List<String>, fpSwitch : Boolean, faceSwitch : Boolean): Thread(){

        var context = context
        var logBox = logBox //unsafe reference to view
        var randomEnrollList = randomEnrollList
        fun log(msg: String) = logBox.append("==> $msg\n")
        var thread_counter = 0
        var doneEnroll: Boolean = true
        var fpSwitch = fpSwitch
        var faceSwitch = faceSwitch

        var bw :BufferedWriter? = null

        override fun run(){
            bw = openCSV("Enroll.csv")
            log("Start Enroll")
            while (thread_counter < randomEnrollList.size) {
                if (doneEnroll){
                    doneEnroll = false
                    //init data
                    if (thread_counter >= randomEnrollList.size) {
                        break
                    }
                    var iD = randomEnrollList[thread_counter].toInt()
                    var enrollFPRecord = Array<FingerprintRecord?>(10) { null }
                    var facePath = "EnrollBMPImages/${iD}/face.jpg"
                    var i = 0

                    //Create FP record
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$iD record is being created")
                    if (fpSwitch) {
                        for ((key, fp) in fpMapping) {
                            val fpPath = "EnrollBMPImages/${iD}/${key}"
                            val fpBmp = this.getBitmapFromSrc(context, fpPath)
                            enrollFPRecord[i] = FingerprintRecord(fp, fpBmp)
                            i += 1
                        }
                    }
                    var enrollFaceRecord : FaceRecord? = null
                    //Create Face record
                    if (faceSwitch) {
                        enrollFaceRecord = FaceRecord(getBitmapFromSrc(context, facePath))
                    }
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
            log("Finished enroll")
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

    class matchThread(context: Context, logBox: TextView, compareList: List<String>, fpSwitch: Boolean, faceSwitch: Boolean): Thread(){

        var context = context
        var logBox = logBox //unsafe reference to view
        var compareList = compareList
        fun log(msg: String) = logBox.append("==> $msg\n")
        var thread_counter = 0
        var doneEnroll: Boolean = true
        var fpSwitch = fpSwitch
        var faceSwitch = faceSwitch

        var bw :BufferedWriter? = null

        override fun run(){
            bw = openCSV("match.csv")
            log("Start match")
            while (thread_counter != compareList.size) {
                if (doneEnroll){
                    doneEnroll = false
                    //init data
                    if (thread_counter >= compareList.size) {
                        break
                    }
                    var iD = compareList[thread_counter].toInt()
                    var compareFPRecord = Array<FingerprintRecord?>(10) { null }
                    var facePath = "CompareBMPImages/${iD}/face.jpg"
                    var i = 0

                    //Create FP record
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$iD record is being created")
                    if (fpSwitch) {
                        for ((key, fp) in fpMapping) {
                            val fpPath = "CompareBMPImages/${iD}/${key}"
                            val fpBmp = this.getBitmapFromSrc(context, fpPath)
                            compareFPRecord[i] = FingerprintRecord(fp, fpBmp)
                            i += 1
                        }
                    }

                    //Create Face record
                    var compareFaceRecord : FaceRecord? = null
                    if (faceSwitch){
                        compareFaceRecord = FaceRecord(getBitmapFromSrc(context, facePath))
                    }

                    //Enroll record to biomanager
                    Logger.getLogger(DatabaseActivity::class.java.name).info("$iD is being matched")
                    startTimer()
                    App.BioManager!!.match(
                        compareFPRecord,
                        compareFaceRecord,
                        null
                    ) { status, arrayList ->
                        log("[Status: $status, Match Count: ${arrayList?.size}]")

                        thread_counter = thread_counter.inc()
                        doneEnroll = true
                        val duration = endTimer()
                        writeCSV(bw!!, iD, duration)
                        log("Match to BioManager: ${duration}s")

                        if (null == arrayList) return@match
                        for (item in arrayList) {
                            log("[FP: ${item.fingerprintScore}," + "Face: ${item.faceScore}, Iris: ${item.irisScore}]")
                        }
                    }
                }
            }
            closeCSV(bw!!)
            log("Finished match")
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

