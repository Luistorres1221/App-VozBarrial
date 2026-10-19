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
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.ui.theme.*

private data class FrameItem(val name:String,val category:String,val detail:String,val color:Color,val pale:Color,val acquired:String)

@Composable
fun FramesPage(
    name: String,
    activeFrame: String,
    monedas: Int = 0,
    profilePhoto: String? = null,
    owned: Set<String> = emptySet(),
    onBack: () -> Unit,
    onEquip: (String) -> Unit,
    onOpenStore: () -> Unit = {}
) {
 val frames=listOf(
  FrameItem("Clásico Cívico","Básico","Marco de bienvenida",Color(0xFF07966C),Color(0xFFDDF8EA),"En tu inventario"),
  FrameItem("Guardián Dorado","Legendario","Canjeado hace 2d",Color(0xFFE9A900),Color(0xFFFFF2C4),"Adquirido"),
  FrameItem("Eco Barrio Verde","Raro","Canjeado hace 1 sem",Color(0xFF07966C),Color(0xFFDDF8EA),"Adquirido"),
  FrameItem("Paz Comunitaria","Épico","Canjeado",Color(0xFF2453F5),Color(0xFFE3E9FF),"Adquirido"),
  FrameItem("Ojo Vigilante Neón","Épico","Canjeado",Color(0xFF00A6C7),Color(0xFFDDF8FF),"Adquirido"),
  FrameItem("Escudo Ciudadano","Común","Canjeado hace 2 sem",Color(0xFFE64B13),Color(0xFFFFE7DE),"Adquirido")
 )
 val visibleFrames = frames.filter { it.name in owned }.map { item ->
  item.copy(detail = "Disponible en tu inventario", acquired = "Adquirido")
 }
 Column(Modifier.fillMaxSize().background(VozBackground)) {
  Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=10.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
   Text("‹",Modifier.clickable(onClick=onBack).padding(horizontal=7.dp),fontSize=24.sp,color=VozNavy)
   Text("Mis Marcos",Modifier.weight(1f),fontSize=15.sp,fontWeight=FontWeight.Bold,color=VozNavy)
   Surface(shape=RoundedCornerShape(50.dp),color=Color(0xFFFFF2E7)){Text("🟠 $monedas",Modifier.padding(horizontal=8.dp,vertical=5.dp),fontSize=9.sp,color=Color(0xFF8A5723),fontWeight=FontWeight.Bold)}
   Spacer(Modifier.width(6.dp))
   Text(" V ",color=VozGreen,fontSize=11.sp,fontWeight=FontWeight.Bold)
  }
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
   Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)) {
    Column(Modifier.fillMaxWidth().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
     Box(contentAlignment = Alignment.BottomEnd) {
      UserAvatarWithFrame(
       profilePhoto = profilePhoto,
       name = name,
       frameName = activeFrame,
       size = 82.dp
      )
      Surface(Modifier.size(26.dp),shape=CircleShape,color=VozNavy){Box(contentAlignment=Alignment.Center){Text("✓",color=Color.White,fontSize=12.sp)}}
     }
     Text(name,color=VozNavy,fontSize=13.sp,fontWeight=FontWeight.Bold)
     Surface(shape=RoundedCornerShape(50.dp),color=Color(0xFF90F3C2)){Text(if(activeFrame.isBlank())"Perfil al natural" else "✓ Marco Activo: $activeFrame",Modifier.padding(horizontal=8.dp,vertical=4.dp),fontSize=9.sp,color=VozGreen,fontWeight=FontWeight.Bold)}
     Row(Modifier.fillMaxWidth().padding(top=3.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
      FrameAction("×  Desequipar",Modifier.weight(1f),true){onEquip("")}
     }
    }
   }
   Surface(Modifier.fillMaxWidth().clickable(onClick = onOpenStore),shape=RoundedCornerShape(50.dp),color=Color.White,shadowElevation=1.dp) {Text("Ir a la Tienda",Modifier.padding(horizontal=12.dp,vertical=7.dp),fontSize=10.sp,color=VozNavy,fontWeight=FontWeight.SemiBold)}
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Marcos Adquiridos",Modifier.weight(1f),color=VozNavy,fontSize=15.sp,fontWeight=FontWeight.Bold)
    Text("${visibleFrames.size} en tu inventario",color=Color(0xFF68788B),fontSize=9.sp)
    Spacer(Modifier.width(5.dp))
   }
   if (visibleFrames.isEmpty()) {
    Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(12.dp)) {
     Text("No tienes marcos en tu inventario. ¡Canjea algunos en la tienda!", Modifier.padding(18.dp), color = VozMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
   } else {
    visibleFrames.chunked(2).forEach { row ->
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
      row.forEach { frame->FrameCard(frame,activeFrame==frame.name,true,Modifier.weight(1f),onClick={onEquip(frame.name)}) }
      if(row.size==1)Spacer(Modifier.weight(1f))
     }
    }
   }
  }
 }
}

@Composable private fun FrameCard(frame:FrameItem,active:Boolean,owned:Boolean,modifier:Modifier,onClick:()->Unit) {
 Card(modifier,colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)) {
  Column(Modifier.fillMaxWidth().padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    Surface(shape=RoundedCornerShape(50.dp),color=frame.pale){Text(frame.category,Modifier.padding(horizontal=5.dp,vertical=3.dp),fontSize=8.sp,color=frame.color,fontWeight=FontWeight.Bold)}
    Spacer(Modifier.weight(1f));Text(frame.acquired,fontSize=7.sp,color=Color(0xFF68788B))
   }
   Box(Modifier.padding(vertical=3.dp).size(52.dp).border(2.dp,frame.color,CircleShape).padding(4.dp).border(1.dp,frame.color,CircleShape).padding(4.dp).background(VozBlueSurface,CircleShape),contentAlignment=Alignment.Center){Text("✦",fontSize=20.sp,color=frame.color)}
   Text(frame.name,color=VozNavy,fontWeight=FontWeight.Bold,fontSize=10.sp,textAlign=TextAlign.Center,maxLines=1)
   Text(frame.detail,color=Color(0xFF68788B),fontSize=8.sp,textAlign=TextAlign.Center,maxLines=1)
   Button(onClick=onClick,enabled=owned,modifier=Modifier.fillMaxWidth().height(31.dp),shape=RoundedCornerShape(7.dp),contentPadding=PaddingValues(2.dp),colors=ButtonDefaults.buttonColors(containerColor=if(active)Color(0xFF90F3C2) else VozNavy,contentColor=if(active)VozGreen else Color.White,disabledContainerColor=VozBlueSurface,disabledContentColor=Color(0xFF68788B))) {Text(if(active)"✓ En Uso" else "Equipar",fontSize=9.sp,fontWeight=FontWeight.Bold)}
  }
 }
}
@Composable private fun FrameAction(label:String,modifier:Modifier,primary:Boolean,onClick:()->Unit){Surface(modifier.height(31.dp).clickable(onClick = onClick),shape=RoundedCornerShape(7.dp),color=if(primary)VozBlueSurface else VozBlueSurface){Box(contentAlignment=Alignment.Center){Text(label,color=VozNavy,fontSize=8.sp,fontWeight=FontWeight.SemiBold)}}}
