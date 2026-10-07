package com.vozbarrial.features.dashboard
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private val N=Color(0xFF0B1C30); private val G=Color(0xFF006C49); private val BG=Color(0xFFF8F9FF)
@Composable fun CommunityPage(page:String,name:String,onBack:()->Unit,onNavigate:(String)->Unit,onSignOut:()->Unit,onSaveName:(String)->Unit={},onDeleteAccount:()->Unit={onSignOut()},profilePhoto:String?=null,points:Int=1450){
 var fullName by remember(name){mutableStateOf(name.trim().substringBefore('%').ifBlank{"Vecino"})}
 var dialog by rememberSaveable{mutableStateOf("")}
 var draft by remember(fullName){mutableStateOf(fullName)}
 var filter by rememberSaveable{mutableStateOf("Todas")}
 var activityFilter by rememberSaveable{mutableStateOf("Todas")}
 var criticalDismissed by rememberSaveable{mutableStateOf(false)}
 var replyText by rememberSaveable{mutableStateOf("")}
 var marks by remember(points){mutableIntStateOf(points)}
 var edited by rememberSaveable{mutableStateOf(false)}
 var resolved by rememberSaveable{mutableStateOf(false)}
 Column(Modifier.fillMaxSize().background(BG)){
  Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=12.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){
   Text("‹",Modifier.clickable{onBack()}.padding(horizontal=6.dp),color=Color(0xFF001428),fontSize=23.sp)
   Surface(Modifier.size(25.dp),shape=CircleShape,color=Color(0xFF001428)){Box(contentAlignment=Alignment.Center){Text("V",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold)}}
   Text(when(page){"profile"->"Perfil Ciudadano";"activity"->"Actividad";else->"Insignias y Logros"},Modifier.weight(1f).padding(start=5.dp),color=Color(0xFF001428),fontWeight=FontWeight.SemiBold,fontSize=14.sp)
   Surface(Modifier.clickable{onNavigate("activity")},shape=CircleShape,color=Color.Transparent){Box(contentAlignment=Alignment.TopEnd){Icon(Icons.Default.Notifications,null,tint=Color(0xFF43474D),modifier=Modifier.padding(6.dp).size(20.dp));Text("3",Modifier.background(Color(0xFFE36D15),CircleShape).padding(horizontal=4.dp),color=Color.White,fontSize=8.sp)}}
  }
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=7.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   when(page){
    "profile"->{
     Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp)){
      Column(Modifier.fillMaxWidth().padding(10.dp)){
       Row(verticalAlignment=Alignment.CenterVertically){
        ProfileAvatar(fullName,profilePhoto,Modifier.size(58.dp));
        Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(fullName,color=N,fontWeight=FontWeight.Bold,fontSize=15.sp);Text("🟢 Nivel 3",color=G,fontSize=11.sp)}
       }
       Spacer(Modifier.height(8.dp))
       Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
        SmallAction("✎  Editar", Modifier.weight(1f)){onNavigate("editProfile")}
        SmallAction("▣  Mis Marcos", Modifier.weight(1f)){onNavigate("frames")}
        SmallAction("▦  Tienda", Modifier.weight(1f)){onNavigate("store")}
       }
      }
     }
     Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)){
      Column(Modifier.fillMaxWidth().padding(11.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
       Row(verticalAlignment=Alignment.CenterVertically){Text("🟢 Nivel 3: Guardián",Modifier.weight(1f),fontWeight=FontWeight.Bold,color=N,fontSize=12.sp);Pill("$marks Pts")}
       Text("Progreso actual                                      550 pts para Héroe",fontSize=9.sp,color=N)
       Box(Modifier.fillMaxWidth().height(9.dp).background(Color(0xFFE5EEFF),RoundedCornerShape(50.dp)).padding(1.dp)){Box(Modifier.fillMaxWidth(.72f).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFF006C49),Color(0xFF77DAAA),Color(0xFFE36D15))),RoundedCornerShape(50.dp)))}
       Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("801 pts",fontSize=8.sp,color=N);Text("72% completado",fontSize=8.sp,color=G);Text("2,000 pts",fontSize=8.sp,color=N)}
       LevelRow("1. Novato","0 - 250 pts","Completado")
       LevelRow("2. Colaborador","251 - 800 pts","Completado")
       LevelRow("3. Guardián","801 - 2,000 pts","Actual")
       LevelRow("4. Héroe Comunitario","2,001+ pts","Próximo")
      }
     }
     Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
      Metric("12","Reportes creados","Total publicados",Modifier.weight(1f))
      Metric("9","Verificados","75% efectividad",Modifier.weight(1f))
      Metric("148","Votos vecinales","Participación",Modifier.weight(1f))
     }
      Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(13.dp)){
       Column(Modifier.fillMaxWidth().clickable{onNavigate("badges")}.padding(10.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){Text("🏅",fontSize=22.sp);Spacer(Modifier.width(7.dp));Column(Modifier.weight(1f)){Text("Insignias y Logros Cívicos",fontWeight=FontWeight.Bold,color=N,fontSize=12.sp);Text("4/5 Desbloqueadas",color=G,fontSize=10.sp)} }
        Text("Descubre tus medallas ganadas, progreso y recompensas por participar en tu barrio.",color=Color(0xFF43474D),fontSize=10.sp)
        Text("Ver todas mis insignias e hitos  →",Modifier.background(Color(0xFFE5EEFF),RoundedCornerShape(50.dp)).padding(horizontal=10.dp,vertical=5.dp),color=Color(0xFF001428),fontSize=9.sp,fontWeight=FontWeight.SemiBold)
       }
      }
     Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(13.dp)){
      Column(Modifier.fillMaxWidth().padding(9.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
       Row(verticalAlignment=Alignment.CenterVertically){Text("Mis Publicaciones Recientes",Modifier.weight(1f),color=N,fontWeight=FontWeight.Bold,fontSize=12.sp);Text("12 en total",color=Color.Gray,fontSize=9.sp)}
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("Todas (12)","Activas (3)","Resueltas (8)","En Revisión (1)").forEach{label->val key=label.substringBefore(" ");val selected=filter==key;Surface(Modifier.clickable{filter=key},color=if(selected)Color(0xFF001428) else Color(0xFFE5EEFF),shape=RoundedCornerShape(50.dp)){Text(label,Modifier.padding(horizontal=9.dp,vertical=5.dp),fontSize=9.sp,color=if(selected)Color.White else Color(0xFF43474D))}}}
       if(filter=="Todas"||filter=="Activas"||filter=="En"){
        Publication("Robo de luminarias en San Martín","Hace 2 horas · Alumbrado Público","Poste #44 sin iluminación tras corte nocturno del cableado general.","RECHAZADO",Color(0xFFFFE5E5),onEdit={dialog="publication"},onDelete={dialog="delete"})
       }
       if(filter=="Todas"||filter=="Resueltas"){
        Publication("Bache en Carrera 15","Hace 3 días · Vía Pública","Cuadrilla municipal asfaltó la zona tras validación vecinal con 28 apoyos.","RESUELTO",Color(0xFFDDF8EA),onEdit={dialog="publication"},onDelete={dialog="delete"})
       }
      }
     }
     AccountOption("Cerrar sesión",false,onSignOut)
     AccountOption("Eliminar cuenta",true){dialog="deleteAccount"}
    }
    "activity"->{
     Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("Todas (7)","Alertas (2)","Verificaciones (5)").forEach{label->val key=label.substringBefore(" (");val selected=activityFilter==key;Surface(Modifier.clickable{activityFilter=key},shape=RoundedCornerShape(50.dp),color=if(selected)Color(0xFF001428) else Color.White){Text(label,Modifier.padding(horizontal=10.dp,vertical=7.dp),fontSize=10.sp,color=if(selected)Color.White else N,fontWeight=FontWeight.SemiBold)}}}
     Text(if(activityFilter=="Todas")"HOY · 4 alertas" else if(activityFilter=="Alertas")"HOY · 2 alertas" else "HOY · 2 verificaciones",color=Color(0xFF43474D),fontSize=10.sp,fontWeight=FontWeight.Bold)
     if((activityFilter=="Todas"||activityFilter=="Alertas")&&!criticalDismissed)ActivityCard("Alerta Crítica · 250 m","Hace 15 min","Robo de luminarias en Parque Central. Comunidad reporta cables cortados y luminarias sustraídas en el sector poniente. Circula con extrema precaución.","ALERTA CRÍTICA",Color(0xFFFFE5D8),"Ver Reporte","Ignorar",{dialog="activityDetail"},{criticalDismissed=true})
     if(activityFilter=="Todas"||activityFilter=="Verificaciones")ActivityCard("¡Tu reporte fue validado por moderadores!","Hace 40 min","Incidencia: Bache profundo en Av. Las Palmas. Tuvo atención de servicios urbanos.","REPORTE VERIFICADO",Color(0xFF90F3C2),"+50 Puntos cívicos sumados",null,{marks+=50},{})
     if(activityFilter=="Todas")ActivityCard("Elena P.","Hace 1 h","Comentó en tu reporte de Alumbrado Carrera 14: “Continúa, pasó anoche y la zona sigue oscura. Ya avisé al grupo de vecinos del bloque 3.”","COMENTARIO",Color(0xFFE5EEFF),"Responder",null,{dialog="reply"},{})
     if(activityFilter=="Todas"||activityFilter=="Alertas")ActivityCard("¡25 vecinos respaldaron tu reporte!","Hace 3 h","Tu reporte sobre el semáforo intermitente ha alcanzado el umbral para ser enviado con carácter prioritario al área de Movilidad.","ALTA PRIORIDAD",Color(0xFFDCE9FF),"Ver reporte",null,{dialog="activityDetail"},{})
     Text("ANTERIORES · 2 registros",color=Color(0xFF43474D),fontSize=10.sp,fontWeight=FontWeight.Bold)
     if(activityFilter=="Todas"||activityFilter=="Verificaciones")ActivityCard("Fuga de agua reparada en Calle 38","Hace 1 día","Cuadrilla municipal de Aguas resolvió los trabajos de sellado y restauración de pavimento. Gracias a tu oportuno aviso.","CASO RESUELTO",Color(0xFF90F3C2),null,null,{},{})
     if(activityFilter=="Todas")ActivityCard("¡Ascendiste a Guardián Comunitario!","Hace 2 días","Has desbloqueado la insignia dorada “Ojo Ciudadano” por verificar más de 10 incidencias activas en tu comunidad.","NUEVO RANGO",Color(0xFFFFDBC9),"Insignia Ojo Ciudadano","Ver vitrina",{onNavigate("badges")},{onNavigate("badges")})
    }
    else->{
     Card(colors=CardDefaults.cardColors(containerColor=N)){Column(Modifier.fillMaxWidth().padding(16.dp)){Text("Guardián · Nivel 3",color=Color.White,fontWeight=FontWeight.Bold,fontSize=20.sp);Text("Te faltan 550 pts para Héroe Comunitario",color=Color.White);LinearProgressIndicator(progress={.72f},modifier=Modifier.fillMaxWidth(),color=Color(0xFF55E0B0))}}
     Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("Todas (6)","Desbloqueadas (4)","Bloqueadas (1)").forEach{Text(it,Modifier.background(Color.White,RoundedCornerShape(20.dp)).padding(7.dp),color=N,fontSize=9.sp)}}
     Tile("🌱 Primer Reporte","+50 pts · Desbloqueada · Publicaste el primer incidente del barrio.")
     Tile("👁 Ojo Ciudadano","+200 pts · Desbloqueada · 10 reportes verificados.")
     Tile("🤝 Buen Vecino","+150 pts · Desbloqueada · 50 comentarios útiles.")
     Tile("🐾 Amigo Animal","+100 pts · Desbloqueada · Ayudaste a una mascota.")
     Tile("🏆 Héroe del Mes","+500 pts · En curso · Top en votos vecinales.")
    }
   }
  }
  if(page!="profile") Row(Modifier.fillMaxWidth().background(Color.White),horizontalArrangement=Arrangement.SpaceEvenly){TextButton({onNavigate("profile")}){Text("Perfil")};TextButton({onNavigate("activity")}){Text("Actividad")};TextButton({onNavigate("badges")}){Text("Insignias")}}
 }
 if(dialog=="edit")AlertDialog(onDismissRequest={dialog=""},title={Text("Editar perfil",color=N)},text={OutlinedTextField(value=draft,onValueChange={draft=it},label={Text("Nombre completo")},singleLine=true)},confirmButton={TextButton(onClick={val valid=draft.trim();if(valid.isNotBlank()){fullName=valid;onSaveName(valid)};dialog=""}){Text("Guardar")}},dismissButton={TextButton(onClick={dialog=""}){Text("Cancelar")}})
 if(dialog=="activityDetail")AlertDialog(onDismissRequest={dialog=""},title={Text("Detalle del reporte",color=N)},text={Text("Robo de luminarias en Parque Central · alerta comunitaria prioritaria. Consulta la ubicación y los comentarios de los vecinos en el mapa.")},confirmButton={TextButton(onClick={dialog=""}){Text("Entendido",color=Color(0xFF001428))}})
 if(dialog=="reply")AlertDialog(onDismissRequest={dialog=""},title={Text("Responder a Elena",color=N)},text={OutlinedTextField(value=replyText,onValueChange={replyText=it},label={Text("Tu respuesta")})},confirmButton={TextButton(onClick={dialog=""}){Text("Enviar",color=Color(0xFF006C49))}},dismissButton={TextButton(onClick={dialog=""}){Text("Cancelar",color=Color(0xFF001428))}})
 if(dialog=="store")AlertDialog(onDismissRequest={dialog=""},title={Text("Tienda vecinal",color=N)},text={Text("Tus $marks puntos están disponibles para canjear beneficios de la comunidad.")},confirmButton={TextButton(onClick={dialog=""}){Text("Entendido")}})
 if(dialog=="publication")AlertDialog(onDismissRequest={dialog=""},title={Text("Editar publicación",color=N)},text={Text("Puedes editar el título y la descripción de tu reporte desde Mis reportes.")},confirmButton={TextButton(onClick={edited=true;dialog=""}){Text(if(edited)"Guardado" else "Listo")}})
 if(dialog=="delete")AlertDialog(onDismissRequest={dialog=""},title={Text("Eliminar publicación",color=N)},text={Text("¿Deseas eliminar este reporte de tu lista?")},confirmButton={TextButton(onClick={resolved=true;dialog=""}){Text("Eliminar")}},dismissButton={TextButton(onClick={dialog=""}){Text("Cancelar")}})
 if(dialog=="deleteAccount")AlertDialog(onDismissRequest={dialog=""},title={Text("Eliminar cuenta",color=N)},text={Text("La cuenta y sus datos guardados en este dispositivo se eliminarán. ¿Deseas continuar?")},confirmButton={TextButton(onClick=onDeleteAccount){Text("Eliminar cuenta",color=Color.Red)}},dismissButton={TextButton(onClick={dialog=""}){Text("Cancelar")}})
}
@Composable private fun SmallAction(label:String,modifier:Modifier=Modifier,onClick:()->Unit){Surface(modifier.height(31.dp).clickable(onClick = onClick),color=Color(0xFFDCE9FF),shape=RoundedCornerShape(7.dp)){Box(contentAlignment=Alignment.Center){Text(label,fontSize=9.sp,color=N,fontWeight=FontWeight.SemiBold)}}}
@Composable private fun Pill(text:String){Surface(color=Color(0xFFE5EEFF),shape=RoundedCornerShape(30.dp)){Text(text,Modifier.padding(horizontal=7.dp,vertical=4.dp),fontSize=9.sp,color=N,fontWeight=FontWeight.Bold)}}
@Composable private fun LevelRow(title:String,points:String,state:String){Row(Modifier.fillMaxWidth().padding(vertical=2.dp).background(if(state=="Actual")Color(0xFFDCE9FF) else Color(0xFFF5F8FB),RoundedCornerShape(7.dp)).padding(horizontal=7.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),fontSize=9.sp,color=N,fontWeight=FontWeight.SemiBold);Text(points,Modifier.weight(1f),fontSize=8.sp,color=Color.Gray);Text(state,fontSize=8.sp,color=if(state=="Actual")N else G,fontWeight=FontWeight.Bold)}}
@Composable private fun Metric(value:String,label:String,sub:String,modifier:Modifier=Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp)){Column(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(value,fontSize=17.sp,color=N,fontWeight=FontWeight.Bold);Text(label,fontSize=9.sp,color=G,fontWeight=FontWeight.SemiBold);Text(sub,fontSize=7.sp,color=Color.Gray)}}}
@Composable private fun Publication(title:String,time:String,description:String,status:String,statusColor:Color,onEdit:()->Unit,onDelete:()->Unit){Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFEFF4FF)),shape=RoundedCornerShape(10.dp)){Column(Modifier.fillMaxWidth().padding(8.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("●",color=G,fontSize=10.sp);Spacer(Modifier.width(5.dp));Text(title,Modifier.weight(1f),color=N,fontWeight=FontWeight.Bold,fontSize=10.sp);Text(status,Modifier.background(statusColor,RoundedCornerShape(20.dp)).padding(horizontal=5.dp,vertical=2.dp),fontSize=7.sp,color=N,fontWeight=FontWeight.Bold)};Text(time,color=Color.Gray,fontSize=8.sp);Text(description,color=N,fontSize=9.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick=onEdit,colors=ButtonDefaults.textButtonColors(contentColor=Color(0xFF001428)),contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp)){Text("Editar",fontSize=9.sp)};TextButton(onClick=onDelete,colors=ButtonDefaults.textButtonColors(contentColor=Color(0xFFBA1A1A)),contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp)){Text("Eliminar",fontSize=9.sp,color=Color.Red)}}}}}
@Composable private fun Tile(t:String,d:String){Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)){Column(Modifier.fillMaxWidth().padding(13.dp)){Text(t,color=N,fontWeight=FontWeight.Bold);Spacer(Modifier.height(4.dp));Text(d,color=Color(0xFF5C6877),fontSize=13.sp)}}}

@Composable private fun AccountOption(label:String,danger:Boolean,onClick:()->Unit){Surface(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(10.dp),color=if(danger)Color(0xFFFFDAD6).copy(alpha=.42f) else Color(0xFFEFF4FF)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Text(if(danger)"⊗" else "⇥",Modifier.background(if(danger)Color(0xFFFFDAD6) else Color(0xFFE5EEFF),CircleShape).padding(8.dp),color=if(danger)Color(0xFFBA1A1A) else Color(0xFF43474D));Spacer(Modifier.width(9.dp));Text(label,Modifier.weight(1f),color=if(danger)Color(0xFFBA1A1A) else Color(0xFF0B1C30),fontWeight=FontWeight.Bold,fontSize=11.sp);Text("›",color=if(danger)Color(0xFFBA1A1A) else Color(0xFF43474D))}}}

@Composable private fun ActivityCard(title:String,time:String,body:String,badge:String,badgeColor:Color,primary:String?,secondary:String?,onPrimary:()->Unit,onSecondary:()->Unit){Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(12.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)){Column(Modifier.fillMaxWidth().padding(9.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("●",Modifier.background(badgeColor,CircleShape).padding(7.dp),color=if(badge.contains("VERIFICADO")||badge.contains("RESUELTO"))Color(0xFF006C49) else Color(0xFFE36D15),fontSize=10.sp);Spacer(Modifier.width(7.dp));Column(Modifier.weight(1f)){Text(badge,Modifier.background(badgeColor,RoundedCornerShape(20.dp)).padding(horizontal=6.dp,vertical=2.dp),fontSize=8.sp,color=Color(0xFF001428),fontWeight=FontWeight.Bold);Text(title,color=Color(0xFF0B1C30),fontSize=11.sp,fontWeight=FontWeight.Bold)};Text(time,color=Color(0xFF43474D),fontSize=8.sp)};Text(body,color=Color(0xFF43474D),fontSize=10.sp,lineHeight=14.sp);if(primary!=null||secondary!=null)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){if(primary!=null)TextButton(onClick=onPrimary,colors=ButtonDefaults.textButtonColors(containerColor=Color(0xFF001428),contentColor=Color.White),contentPadding=PaddingValues(horizontal=9.dp,vertical=0.dp)){Text(primary,fontSize=8.sp)};if(secondary!=null)TextButton(onClick=onSecondary,colors=ButtonDefaults.textButtonColors(containerColor=Color(0xFFE5EEFF),contentColor=Color(0xFF001428)),contentPadding=PaddingValues(horizontal=9.dp,vertical=0.dp)){Text(secondary,fontSize=8.sp)}}}}}

@Composable private fun ProfileAvatar(name:String,photo:String?,modifier:Modifier){val context=LocalContext.current;val bitmap=remember(photo){runCatching{photo?.let{context.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream)?.asImageBitmap()}}.getOrNull()};if(bitmap!=null)Image(bitmap,contentDescription="Foto de perfil",modifier=modifier.clip(CircleShape),contentScale=ContentScale.Crop)else Box(modifier.background(Color(0xFFE4F2EA),CircleShape),contentAlignment=Alignment.Center){Text(name.take(1).uppercase(),fontSize=22.sp,color=G,fontWeight=FontWeight.Bold)}}
