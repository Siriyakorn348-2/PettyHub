package com.neatroots.newdog



data class DecisionNode(
    val question: String? = null,
    val yesNode: DecisionNode? = null,
    val noNode: DecisionNode? = null,
    val diagnosis: String? = null,
    val details: String? = null,
    val careInstructions: String? = null,
    val symptomsAndCauses: String? = null
)

