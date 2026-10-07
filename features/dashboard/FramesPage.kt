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

private val FrameNavy=Color(0xFF001428)
private val FrameGreen=Color(0xFF006C49)
private val FrameBg=Color(0xFFF8F9FF)
private data class FrameItem(val name:String,val category:String,val detail:String,val color:Color,val pale:Color,val acquired:String)

@Composable
fun FramesPage(name:String,activeFrame:String,points:Int=1450,onBack:()->Unit,onEquip:(String)->Unit) {
 var showStore by remember { mutableStateOf(false) }
 val frames=listOf(
  FrameItem("Clásico Cívico","Básico","Marco de bienvenida",Color(0xFF07966C),Color(0xFFDDF8EA),"En tu inventario"),
  FrameItem("Guardián Dorado","Legendario","Canjeado hace 2d",Color(0xFFE9A900),Color(0xFFFFF2C4),"Adquirido"),
  FrameItem("Eco Barrio Verde","Raro","Canjeado hace 1 sem",Color(0xFF07966C),Color(0xFFDDF8EA),"Adquirido"),
  FrameItem("Escudo Ciudadano","Común","Canjeado hace 2 sem",Color(0xFFE64B13),Color(0xFFFFE7DE),"Adquirido")
 )
 Column(Modifier.fillMaxSize().background(FrameBg)) {
  Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=10.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
   Text("‹",Modifier.clickable(onClick=onBack).padding(horizontal=7.dp),fontSize=24.sp,color=FrameNavy)
   Text("Mis Marcos",Modifier.weight(1f),fontSize=15.sp,fontWeight=FontWeight.Bold,color=FrameNavy)
   Surface(shape=RoundedCornerShape(50.dp),color=Color(0xFFE5EEFF)){Text("🟠 "+points+" Pts",Modifier.padding(horizontal=8.dp,vertical=5.dp),fontSize=9.sp,color=FrameNavy,fontWeight=FontWeight.Bold)}
   Text(" V ",color=FrameGreen,fontSize=11.sp,fontWeight=FontWeight.Bold)
  }
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
   Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)) {
    Column(Modifier.fillMaxWidth().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
     Box(Modifier.size(82.dp).border(2.dp,if(activeFrame.isBlank())Color(0xFFCAD2DC) else frames.firstOrNull{it.name==activeFrame}?.color?:FrameGreen,CircleShape).padding(5.dp).border(2.dp,if(activeFrame.isBlank())Color(0xFFCAD2DC) else frames.firstOrNull{it.name==activeFrame}?.color?:FrameGreen,CircleShape).padding(4.dp).background(Color(0xFFE5EEFF),CircleShape),contentAlignment=Alignment.Center) {
      Text(name.split(" ").take(2).joinToString("\n"),modifier=Modifier.padding(6.dp),fontSize=11.sp,color=FrameNavy,textAlign=TextAlign.Center)
      Surface(Modifier.align(Alignment.BottomEnd).size(26.dp),shape=CircleShape,color=FrameNavy){Box(contentAlignment=Alignment.Center){Text("✓",color=Color.White,fontSize=12.sp)}}
     }
     Text(name,color=FrameNavy,fontSize=13.sp,fontWeight=FontWeight.Bold)
     Surface(shape=RoundedCornerShape(50.dp),color=Color(0xFF90F3C2)){Text(if(activeFrame.isBlank())"Perfil al natural" else "✓ Marco Activo: $activeFrame",Modifier.padding(horizontal=8.dp,vertical=4.dp),fontSize=9.sp,color=FrameGreen,fontWeight=FontWeight.Bold)}
     Row(Modifier.fillMaxWidth().padding(top=3.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
      FrameAction("⊘  Perfil al natural",Modifier.weight(1f),false){onEquip("")}
      FrameAction("×  Desequipar",Modifier.weight(1f),true){onEquip("")}
     }
    }
   }
   Surface(Modifier.fillMaxWidth().clickable{showStore=true},shape=RoundedCornerShape(50.dp),color=Color.White,shadowElevation=1.dp) {Text("▣  Ir a la Tienda",Modifier.padding(horizontal=12.dp,vertical=7.dp),fontSize=10.sp,color=FrameNavy,fontWeight=FontWeight.SemiBold)}
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Marcos Adquiridos",Modifier.weight(1f),color=FrameNavy,fontSize=15.sp,fontWeight=FontWeight.Bold)
    Text("4 de 6 en total",color=Color(0xFF68788B),fontSize=9.sp)
    Spacer(Modifier.width(5.dp))
    Surface(shape=RoundedCornerShape(50.dp),color=Color(0xFF90F3C2)){Text("✓ En tu inventario",Modifier.padding(horizontal=7.dp,vertical=4.dp),color=FrameGreen,fontSize=8.sp,fontWeight=FontWeight.Bold)}
   }
   frames.chunked(2).forEach { row ->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
     row.forEach { frame->FrameCard(frame,activeFrame==frame.name,Modifier.weight(1f),onClick={onEquip(frame.name)}) }
     if(row.size==1)Spacer(Modifier.weight(1f))
    }
   }
  }
 }
 if(showStore)AlertDialog(onDismissRequest={showStore=false},title={Text("Tienda de Marcos",color=FrameNavy)},text={Text("Tienes "+points+" puntos. Pronto podrás canjearlos por nuevos marcos para tu perfil.")},confirmButton={TextButton(onClick={showStore=false}){Text("Entendido",color=FrameGreen)}})
}

@Composable private fun FrameCard(frame:FrameItem,active:Boolean,modifier:Modifier,onClick:()->Unit) {
 Card(modifier,colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)) {
  Column(Modifier.fillMaxWidth().padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    Surface(shape=RoundedCornerShape(50.dp),color=frame.pale){Text(frame.category,Modifier.padding(horizontal=5.dp,vertical=3.dp),fontSize=8.sp,color=frame.color,fontWeight=FontWeight.Bold)}
    Spacer(Modifier.weight(1f));Text(frame.acquired,fontSize=7.sp,color=Color(0xFF68788B))
   }
   Box(Modifier.padding(vertical=3.dp).size(52.dp).border(2.dp,frame.color,CircleShape).padding(4.dp).border(1.dp,frame.color,CircleShape).padding(4.dp).background(Color(0xFFE5EEFF),CircleShape),contentAlignment=Alignment.Center){Text("✦",fontSize=20.sp,color=frame.color)}
   Text(frame.name,color=FrameNavy,fontWeight=FontWeight.Bold,fontSize=10.sp,textAlign=TextAlign.Center,maxLines=1)
   Text(frame.detail,color=Color(0xFF68788B),fontSize=8.sp,textAlign=TextAlign.Center,maxLines=1)
   Button(onClick=onClick,modifier=Modifier.fillMaxWidth().height(31.dp),shape=RoundedCornerShape(7.dp),contentPadding=PaddingValues(2.dp),colors=ButtonDefaults.buttonColors(containerColor=if(active)Color(0xFF90F3C2) else FrameNavy,contentColor=if(active)FrameGreen else Color.White)) {Text(if(active)"✓ En Uso" else "⊙ Equipar",fontSize=9.sp,fontWeight=FontWeight.Bold)}
  }
 }
}
@Composable private fun FrameAction(label:String,modifier:Modifier,primary:Boolean,onClick:()->Unit){Surface(modifier.height(31.dp).clickable(onClick = onClick),shape=RoundedCornerShape(7.dp),color=if(primary)Color(0xFFEFF4FF) else Color(0xFFE5EEFF)){Box(contentAlignment=Alignment.Center){Text(label,color=FrameNavy,fontSize=8.sp,fontWeight=FontWeight.SemiBold)}}}
