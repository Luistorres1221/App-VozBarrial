package com.vozbarrial.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vozbarrial.R
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.ui.theme.*

private data class CatalogFrame(
    val id: String,
    val nameRes: Int,
    val typeRes: Int,
    val cost: Int,
    val color: Color,
    val pale: Color,
    val locked: Boolean = false
)

@Composable
fun StorePage(
    name: String,
    monedas: Int,
    profilePhoto: String?,
    owned: Set<String>,
    activeFrame: String,
    onBack: () -> Unit,
    onPurchase: (String, Int, (Boolean, String?) -> Unit) -> Unit,
    onEquip: (String, (Boolean, String?) -> Unit) -> Unit
) {
    val items = listOf(
        CatalogFrame("Escudo Ciudadano", R.string.frame_escudo_ciudadano, R.string.rarity_common, 500, Color(0xFFE64B13), Color(0xFFFFE7DE)),
        CatalogFrame("Eco Barrio Verde", R.string.frame_eco_barrio_verde, R.string.rarity_rare, 800, Color(0xFF07966C), Color(0xFFDDF8EA)),
        CatalogFrame("Ojo Vigilante Neón", R.string.frame_ojo_vigilante_neon, R.string.rarity_epic, 1200, Color(0xFF00A6C7), Color(0xFFDDF8FF)),
        CatalogFrame("Paz Comunitaria", R.string.frame_paz_comunitaria, R.string.rarity_epic, 1200, Color(0xFF2453F5), Color(0xFFE3E9FF)),
        CatalogFrame("Guardián Dorado", R.string.frame_guardian_dorado, R.string.rarity_legendary, 1800, Color(0xFFE8A800), Color(0xFFFFF1BC)),
        CatalogFrame("Líder Platino", R.string.frame_lider_platino, R.string.rarity_mythic, 4000, Color(0xFF8996A5), Color(0xFFE9EDF2))
    )
    val unownedItems = items.filter { it.id !in owned }
    var preview by remember(activeFrame) { mutableStateOf(activeFrame) }
    var message by remember { mutableStateOf("") }
    val chosen = items.firstOrNull { it.id == preview }
    val previewDisplayName = getLocalizedFrameName(preview)

    Column(Modifier.fillMaxSize().background(VozBackground)) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clickable(onClick = onBack).padding(6.dp), fontSize = 24.sp, color = VozNavy)
            Text(stringResource(R.string.store), Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VozNavy)
            Surface(shape = RoundedCornerShape(50.dp), color = Color(0xFFFFF2E7)) {
                Text("🟠 $monedas", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), fontSize = 10.sp, color = Color(0xFF8A5723), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            Text(" V ", color = VozGreen, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    UserAvatarWithFrame(
                        profilePhoto = profilePhoto,
                        name = name,
                        frameName = preview,
                        size = 72.dp
                    )
                    Text(name, color = VozNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (preview.isNotBlank() && chosen != null) {
                        Text(stringResource(R.string.testing_frame, previewDisplayName), Modifier.background(chosen.pale, RoundedCornerShape(50.dp)).padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 9.sp, color = chosen.color, fontWeight = FontWeight.Bold)
                    } else {
                        Text(stringResource(R.string.normal_profile_view), Modifier.background(VozBlueSurface, RoundedCornerShape(50.dp)).padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 9.sp, color = VozNavy, fontWeight = FontWeight.Bold)
                    }
                    Text(stringResource(R.string.store_preview_instructions), color = Color(0xFF43474D), fontSize = 9.sp, textAlign = TextAlign.Center)
                    Row(Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StoreAction(stringResource(R.string.unequip), Modifier.weight(1f)) {
                            onEquip("") { ok, error ->
                                if (ok) preview = ""
                            }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.frames_catalog), Modifier.weight(1f), fontSize = 14.sp, color = VozNavy, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.coin_store), Modifier.background(Color(0xFF90F3C2), RoundedCornerShape(40.dp)).padding(6.dp), fontSize = 8.sp, color = VozGreen, fontWeight = FontWeight.Bold)
            }
            if (unownedItems.isEmpty()) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.all_frames_acquired), Modifier.padding(18.dp), color = VozNavy, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                unownedItems.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        row.forEach { item ->
                            val localizedName = getLocalizedFrameName(item.id)
                            CatalogCard(
                                item = item,
                                monedas = monedas,
                                preview = preview == item.id,
                                modifier = Modifier.weight(1f),
                                onTry = { preview = item.id },
                                onAction = {
                                    if (monedas >= item.cost) {
                                        onPurchase(item.id, item.cost) { ok, error ->
                                            message = if (ok) "$localizedName canjeado y equipado." else error ?: "No se pudo completar el canje."
                                            if (ok) preview = item.id
                                        }
                                    } else {
                                        message = "Te faltan " + (item.cost - monedas) + " monedas para $localizedName."
                                    }
                                }
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (message.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { message = "" },
            title = { Text(stringResource(R.string.store_dialog_title), color = VozNavy) },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { message = "" }) { Text(stringResource(R.string.dialog_understood), color = VozGreen) } }
        )
    }
}

@Composable
private fun CatalogCard(item: CatalogFrame, monedas: Int, preview: Boolean, modifier: Modifier, onTry: () -> Unit, onAction: () -> Unit) {
    val itemName = getLocalizedFrameName(item.id)
    val itemType = stringResource(item.typeRes)
    val tryText = stringResource(R.string.try_action)
    val redeemText = stringResource(R.string.redeem)
    val lockedText = stringResource(R.string.locked)

    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(11.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✦ " + itemType, Modifier.background(item.pale, RoundedCornerShape(40.dp)).padding(horizontal = 5.dp, vertical = 3.dp), fontSize = 7.sp, color = item.color, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("🟠 " + item.cost, fontSize = 8.sp, color = VozNavy, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.align(Alignment.CenterHorizontally).size(49.dp).border(2.dp, item.color, CircleShape).padding(4.dp).border(1.dp, item.color, CircleShape).padding(4.dp).background(VozBlueSurface, CircleShape), contentAlignment = Alignment.Center) {
                Text("✦", fontSize = 18.sp, color = item.color)
            }
            Text(itemName, Modifier.fillMaxWidth(), fontSize = 9.sp, color = VozNavy, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            val canAfford = monedas >= item.cost
            if (!canAfford) {
                Text(stringResource(R.string.missing_coins, item.cost - monedas), Modifier.fillMaxWidth().background(Color(0xFFFFE5E5), RoundedCornerShape(4.dp)).padding(3.dp), fontSize = 7.sp, color = Color(0xFFBA1A1A), textAlign = TextAlign.Center)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                OutlinedButton(
                    onClick = onTry,
                    modifier = Modifier.weight(1f).height(27.dp),
                    contentPadding = PaddingValues(1.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VozNavy)
                ) {
                    Text(if (preview) "✓ $tryText" else "⊙ $tryText", fontSize = 7.sp)
                }
                Button(
                    onClick = onAction,
                    enabled = canAfford,
                    modifier = Modifier.weight(1f).height(27.dp),
                    contentPadding = PaddingValues(1.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VozNavy,
                        contentColor = Color.White,
                        disabledContainerColor = VozBlueSurface,
                        disabledContentColor = Color(0xFF68788B)
                    )
                ) {
                    Text(
                        if (canAfford) redeemText else lockedText,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreAction(label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier.height(31.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(7.dp),
        color = VozBlueSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = VozNavy, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
