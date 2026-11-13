package com.vozbarrial.domain.model

import com.vozbarrial.R

data class RelevanceData(val level: String, val severity: Int, val desc: String, val bars: Int)

fun calculateRelevance(votes: Int, getString: (Int, List<Any>) -> String): RelevanceData {
    val min = getString(R.string.relevance_minor, emptyList())
    val low = getString(R.string.relevance_low, emptyList())
    val mod = getString(R.string.relevance_moderate, emptyList())
    val high = getString(R.string.relevance_high, emptyList())
    val crit = getString(R.string.relevance_critical, emptyList())

    return when {
        votes < 20 -> RelevanceData(min, 1, getString(R.string.relevance_desc_format, listOf(min, votes)), 1)
        votes < 40 -> RelevanceData(low, 2, getString(R.string.relevance_desc_format, listOf(low, votes)), 2)
        votes < 60 -> RelevanceData(mod, 3, getString(R.string.relevance_desc_format, listOf(mod, votes)), 3)
        votes < 80 -> RelevanceData(high, 4, getString(R.string.relevance_desc_format, listOf(high, votes)), 4)
        else -> RelevanceData(crit, 5, getString(R.string.relevance_critical_desc, emptyList()), 5)
    }
}
