package com.neatroots.newdog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neatroots.newdog.databinding.ActivityDecisionTreeBinding
import com.neatroots.newdog.databinding.ActivityDecisionTreeResultBinding

class DecisionTreeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDecisionTreeBinding
    private lateinit var tree: DecisionNode
    private var currentNode: DecisionNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecisionTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        tree = DecisionTree().createTree()
        currentNode = tree
        initializeQuestionView(tree)
    }

    private fun initializeQuestionView(node: DecisionNode?) {

        if (node == null || node.question == null) {
            initializeResultView(node ?: DecisionNode())
            return
        }


        binding.questionTextView.text = node.question


        binding.yesButton.setOnClickListener {
            it.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                moveToNextNode(true)
            }.start()
        }


        binding.noButton.setOnClickListener {
            it.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                moveToNextNode(false)
            }.start()
        }
    }

    private fun moveToNextNode(answer: Boolean) {

        val node = currentNode ?: run {
            finish()
            return
        }


        currentNode = if (answer) node.yesNode else node.noNode


        when {
            currentNode != null -> initializeQuestionView(currentNode)
            else -> initializeResultView(node)
        }
    }

    private fun initializeResultView(node: DecisionNode) {
        val resultBinding = ActivityDecisionTreeResultBinding.inflate(layoutInflater)
        setContentView(resultBinding.root)

        resultBinding.back.setOnClickListener { finish() }
        resultBinding.diagnosis.text = node.diagnosis ?: "ไม่ทราบอาการ"
        resultBinding.aboutDescription.text = node.details ?: "ไม่มีรายละเอียดเพิ่มเติม"

        resultBinding.dogCare.setOnClickListener {
            it.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                val intent = Intent(this@DecisionTreeActivity, CareInstructionsActivity::class.java).apply {
                    putExtra("DIAGNOSIS", node.diagnosis)
                    putExtra("CARE_INSTRUCTIONS", node.careInstructions ?: "ไม่มีคำแนะนำเพิ่มเติม")
                    putExtra("SYMPTOMS_AND_CAUSES", node.symptomsAndCauses ?: "ไม่มีข้อมูลสาเหตุและอาการ")
                }
                startActivity(intent)
            }.start()
        }

        resultBinding.hospital.setOnClickListener {
            it.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                val intent = Intent(this@DecisionTreeActivity, CategoryActivity::class.java).apply {
                    putExtra("TITLE", "โรงพยาบาล")
                    putExtra("CATEGORY", "โรงพยาบาล")
                }
                startActivity(intent)
            }.start()
        }
    }
}