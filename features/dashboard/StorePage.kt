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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private val SN=Color(0xFF001428); private val SG=Color(0xFF006C49); private val SB=Color(0xFFF8F9FF)
private data class CatalogFrame(val name:String,val type:String,val cost:Int,val color:Color,val pale:Color,val locked:Boolean=false)
@Composable fun StorePage(name:String,points:Int,owned:Set<String>,activeFrame:String,onBack:()->Unit,onPurchase:(String,Int,Set<String>)->Unit,onEquip:(String)->Unit){
 val items=listOf(CatalogFrame("Guardián Dorado","Legendario",800,Color(0xFFE8A800),Color(0xFFFFF1BC)),CatalogFrame("Eco Barrio Verde","Raro",450,Color(0xFF07966C),Color(0xFFDDF8EA)),CatalogFrame("Ojo Vigilante Neón","Épico",800,Color(0xFF00A6C7),Color(0xFFDDF8FF)),CatalogFrame("Paz Comunitaria","Épico",1200,Color(0xFF2453F5),Color(0xFFE3E9FF)),CatalogFrame("Líder Platino","Mítico",1800,Color(0xFF8996A5),Color(0xFFE9EDF2),true),CatalogFrame("Escudo Ciudadano","Común",500,Color(0xFFE84A12),Color(0xFFFFE4DA)))
 var preview by remember(activeFrame){mutableStateOf(activeFrame.ifBlank{"Guardián Dorado"})};var message by remember{mutableStateOf("")}
 val chosen=items.firstOrNull{it.name==preview}?:items.first()
 Column(Modifier.fillMaxSize().background(SB)){
  Row(Modifier.fillMaxWidth().background(Color.White).padding(10.dp),verticalAlignment=Alignment.CenterVertically){Text("‹",Modifier.clickable(onClick=onBack).padding(6.dp),fontSize=24.sp,color=SN);Text("Tienda",Modifier.weight(1f),fontSize=15.sp,fontWeight=FontWeight.Bold,color=SN);Text("🟠 "+points+" Pts",Modifier.background(Color(0xFFE5EEFF),RoundedCornerShape(40.dp)).padding(horizontal=8.dp,vertical=5.dp),fontSize=9.sp,color=SN,fontWeight=FontWeight.Bold);Text(" V ",color=SG,fontWeight=FontWeight.Bold)}
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
   Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)){Column(Modifier.fillMaxWidth().padding(9.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
    Box(Modifier.size(72.dp).border(2.dp,chosen.color,CircleShape).padding(4.dp).border(2.dp,chosen.color,CircleShape).padding(5.dp).background(Color(0xFFE5EEFF),CircleShape),contentAlignment=Alignment.Center){Text(name.split(" ").take(2).joinToString("\n"),Modifier.padding(6.dp),fontSize=10.sp,color=SN,textAlign=TextAlign.Center)}
    Text(name,color=SN,fontSize=13.sp,fontWeight=FontWeight.Bold);Text("◉ Probando: "+preview,Modifier.background(chosen.pale,RoundedCornerShape(50.dp)).padding(horizontal=8.dp,vertical=3.dp),fontSize=9.sp,color=chosen.color,fontWeight=FontWeight.Bold)
    Text("Toca Probar para ver el marco en directo antes de canjear tus puntos.",color=Color(0xFF43474D),fontSize=9.sp,textAlign=TextAlign.Center)
   }}
   Row(verticalAlignment=Alignment.CenterVertically){Text("Catálogo de Marcos",Modifier.weight(1f),fontSize=14.sp,color=SN,fontWeight=FontWeight.Bold);Text("Tienda de Puntos",Modifier.background(Color(0xFF90F3C2),RoundedCornerShape(40.dp)).padding(6.dp),fontSize=8.sp,color=SG,fontWeight=FontWeight.Bold)}
   items.chunked(2).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){row.forEach{item->CatalogCard(item,points,item.name in owned,preview==item.name,Modifier.weight(1f),onTry={preview=item.name},onAction={if(item.name in owned)onEquip(item.name)else if(points>=item.cost){onPurchase(item.name,item.cost,owned+item.name);onEquip(item.name);message=item.name+" canjeado y equipado."}else message="Te faltan "+(item.cost-points)+" puntos para "+item.name+"."})};if(row.size==1)Spacer(Modifier.weight(1f))}}
  }
 }
 if(message.isNotBlank())AlertDialog(onDismissRequest={message=""},title={Text("Tienda de Marcos",color=SN)},text={Text(message)},confirmButton={TextButton(onClick={message=""}){Text("Entendido",color=SG)}})
}
@Composable private fun CatalogCard(item:CatalogFrame,points:Int,owned:Boolean,preview:Boolean,modifier:Modifier,onTry:()->Unit,onAction:()->Unit){
 Card(modifier,colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(11.dp),elevation=CardDefaults.cardElevation(2.dp)){Column(Modifier.fillMaxWidth().padding(6.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
  Row(verticalAlignment=Alignment.CenterVertically){Text("✦ "+item.type,Modifier.background(item.pale,RoundedCornerShape(40.dp)).padding(horizontal=5.dp,vertical=3.dp),fontSize=7.sp,color=item.color,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("◉ "+item.cost,fontSize=8.sp,color=SN)}
  Box(Modifier.align(Alignment.CenterHorizontally).size(49.dp).border(2.dp,item.color,CircleShape).padding(4.dp).border(1.dp,item.color,CircleShape).padding(4.dp).background(Color(0xFFE5EEFF),CircleShape),contentAlignment=Alignment.Center){Text("✦",fontSize=18.sp,color=item.color)}
  Text(item.name,Modifier.fillMaxWidth(),fontSize=9.sp,color=SN,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,maxLines=1)
  if(item.locked)Text("Faltan "+(item.cost-points)+" Pts",Modifier.fillMaxWidth().background(Color(0xFFFFE5E5),RoundedCornerShape(4.dp)).padding(3.dp),fontSize=7.sp,color=Color(0xFFBA1A1A),textAlign=TextAlign.Center)
  Row(horizontalArrangement=Arrangement.spacedBy(3.dp)){OutlinedButton(onClick=onTry,modifier=Modifier.weight(1f).height(27.dp),contentPadding=PaddingValues(1.dp),shape=RoundedCornerShape(6.dp),colors=ButtonDefaults.outlinedButtonColors(contentColor=SN)){Text(if(preview)"✓ Probar" else "⊙ Probar",fontSize=7.sp)};Button(onClick=onAction,enabled=owned||(!item.locked&&points>=item.cost),modifier=Modifier.weight(1f).height(27.dp),contentPadding=PaddingValues(1.dp),shape=RoundedCornerShape(6.dp),colors=ButtonDefaults.buttonColors(containerColor=if(owned)Color(0xFF90F3C2) else SN,contentColor=if(owned)SG else Color.White,disabledContainerColor=Color(0xFFE5EEFF),disabledContentColor=Color(0xFF68788B))){Text(if(owned)"Equipar" else if(item.locked)"Bloqueado" else "Canjear",fontSize=7.sp,fontWeight=FontWeight.Bold)}}
 }}
}
