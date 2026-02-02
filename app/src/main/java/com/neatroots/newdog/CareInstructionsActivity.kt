package com.neatroots.newdog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.neatroots.newdog.databinding.ActivityCareInstructionsBinding

class CareInstructionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCareInstructionsBinding
    private lateinit var relatedContentAdapter: RelatedContentAdapter
    private lateinit var relatedContentList: ArrayList<RelatedContent>
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCareInstructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // รับข้อมูลจาก Intent
        val diagnosis = intent.getStringExtra("DIAGNOSIS") ?: ""
        val careInstructions = intent.getStringExtra("CARE_INSTRUCTIONS")
        val symptomsAndCauses = intent.getStringExtra("SYMPTOMS_AND_CAUSES")

        Log.d("CareInstructions", "Diagnosis received: $diagnosis")

        // ตั้งค่า UI พื้นฐาน
        binding.back.setOnClickListener { finish() }
        binding.diagnosisTextView.text = "โรค: ${diagnosis.ifEmpty { "ไม่ทราบอาการ" }}"
        binding.symptomsAndCausesTextView.text = symptomsAndCauses ?: "ไม่มีข้อมูลสาเหตุและอาการ"
        binding.careInstructionsTextView.text = careInstructions ?: "ไม่มีคำแนะนำเพิ่มเติม"

        // ตั้งค่ารูปภาพหลักตามโรค (ใช้ Resource ID)
        when (diagnosis) {
            "ไข้หัดสุนัข" -> binding.diagnosisImage.setImageResource(R.drawable.distemper_image)
            "ลำไส้อักเสบติดต่อจากเชื้อพาโรไวรัส" -> binding.diagnosisImage.setImageResource(R.drawable.parvo_image)
            "ไข้หวัดใหญ่ในสุนัข" -> binding.diagnosisImage.setImageResource(R.drawable.flu_image)
            "หลอดลมอักเสบในสุนัข" -> binding.diagnosisImage.setImageResource(R.drawable.bronchitis_image)
            "ไตวายเรื้อรัง" -> binding.diagnosisImage.setImageResource(R.drawable.kidney_image)
            else -> binding.diagnosisImage.setImageResource(R.drawable.placeholder_image)
        }

        // ตั้งค่า Room Database
        db = Room.databaseBuilder(this, AppDatabase::class.java, "datadog.db")
            .fallbackToDestructiveMigration()
            .createFromAsset("datadog.db")
            .build()

        // ดึงข้อมูลเนื้อหาที่เกี่ยวข้อง
        relatedContentList = ArrayList()
        fetchRelatedContent(diagnosis)
    }

    private fun fetchRelatedContent(diagnosis: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = db.getDao()
            val allData = dao.getAll()

            Log.d("CareInstructions", "Total data count: ${allData.size}")
            relatedContentList.clear() // ล้างข้อมูลเก่าก่อน

            // กรองข้อมูลที่เกี่ยวข้องกับโรคหรือการดูแล
            allData.forEach { dog ->
                if (dog != null) {
                    val title = dog.title ?: ""
                    val description = dog.des ?: ""
                    // ตรวจสอบว่า title หรือ description มีคำที่เกี่ยวข้องกับ diagnosis หรือการดูแล
                    if (title.contains(diagnosis, ignoreCase = true) ||
                        description.contains(diagnosis, ignoreCase = true) ||
                        title.contains("การดูแล", ignoreCase = true) ||
                        description.contains("การดูแล", ignoreCase = true) ||
                        title.contains("care", ignoreCase = true) ||
                        description.contains("care", ignoreCase = true) ||
                        title.contains("treatment", ignoreCase = true) ||
                        description.contains("treatment", ignoreCase = true)) {
                        relatedContentList.add(RelatedContent(dog))
                    }
                }
            }

            Log.d("CareInstructions", "Filtered related count: ${relatedContentList.size}")

            // ถ้าไม่มีข้อมูลที่เกี่ยวข้องเลย แสดงข้อความแจ้งเตือนใน UI แทนการสุ่ม
            runOnUiThread {
                if (relatedContentList.isEmpty()) {
                    binding.relatedContentTitle.text = "ไม่มีข้อมูลการดูแลที่เกี่ยวข้อง"
                    binding.relatedContentTitle.visibility = android.view.View.VISIBLE
                    binding.relatedContentRecyclerView.visibility = android.view.View.GONE
                } else {
                    binding.relatedContentTitle.text = "เนื้อหาการดูแลที่เกี่ยวข้อง"
                    setupRelatedContentRecyclerView()
                }
            }
        }
    }

    private fun setupRelatedContentRecyclerView() {
        binding.relatedContentRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        relatedContentAdapter = RelatedContentAdapter(this, relatedContentList) { relatedContent ->
            // เมื่อคลิกการ์ด เปิด ContentActivity
            val intent = Intent(this, ContentActivity::class.java).apply {
                putExtra("title", relatedContent.dogData.title)
                putExtra("des", relatedContent.dogData.des)
                putExtra("breed", relatedContent.dogData.breed)
                putExtra("age", relatedContent.dogData.age)
                putExtra("img", relatedContent.dogData.img)
            }
            startActivity(intent)
        }
        binding.relatedContentRecyclerView.adapter = relatedContentAdapter

        if (relatedContentList.isEmpty()) {
            Log.d("CareInstructions", "No related items, hiding RecyclerView")
            binding.relatedContentTitle.visibility = android.view.View.GONE
            binding.relatedContentRecyclerView.visibility = android.view.View.GONE
        } else {
            Log.d("CareInstructions", "Showing ${relatedContentList.size} related items")
            binding.relatedContentTitle.visibility = android.view.View.VISIBLE
            binding.relatedContentRecyclerView.visibility = android.view.View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}

// ปรับ Data class ให้เก็บ DogData ทั้งหมด
data class RelatedContent(val dogData: DogData)

// ปรับ Adapter ให้ใช้ Glide และรับ Context
class RelatedContentAdapter(
    private val context: Context,
    private val items: List<RelatedContent>,
    private val onItemClick: (RelatedContent) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<RelatedContentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_related_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.dogData.title
        // ใช้ Glide โหลดรูปภาพจาก img (String) ใน DogData
        Glide.with(context)
            .load(item.dogData.img)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.image)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.relatedImage)
        val title: TextView = itemView.findViewById(R.id.relatedTitle)
    }
}