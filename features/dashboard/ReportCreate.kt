package com.vozbarrial.features.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.window.Dialog

private val RN=Color(0xFF06213A); private val RG=Color(0xFF07845D); private val RB=Color(0xFFEEF3FF); private val RM=Color(0xFF68758A)
private data class ReportCategory(val name:String,val description:String,val icon:String)
private val reportCategories = listOf(
 ReportCategory("Seguridad","Robos, vandalismo, actividades sospechosas","🛡"),
 ReportCategory("Emergencias Médicas","Accidentes viales, primeros auxilios","✚"),
 ReportCategory("Infraestructura y Vías","Baches, fallas de alumbrado, alcantarillado","⚒"),
 ReportCategory("Mascotas y Animales","Animales perdidos, encontrados o en peligro","🐾"),
 ReportCategory("Comunidad y Entorno","Basuras, polución o ruido excesivo","♧"),
 ReportCategory("Alumbrado y Servicios","Postes sin luz, fugas de agua o gas","♧"),
)
@Composable fun ReportCreate(name:String,onBack:()->Unit,onPublish:(String,String,String,Double,Double)->Unit){
 var category by rememberSaveable{mutableStateOf("Seguridad")}; var menu by remember{mutableStateOf(false)}
 var title by rememberSaveable{mutableStateOf("")}; var description by rememberSaveable{mutableStateOf("")}; var photo by rememberSaveable{mutableStateOf<String?>(null)}; var error by rememberSaveable{mutableStateOf("")}
 var latitude by rememberSaveable { mutableDoubleStateOf(4.60971) }; var longitude by rememberSaveable { mutableDoubleStateOf(-74.08175) }; var gpsActive by rememberSaveable { mutableStateOf(false) }; var locationSelected by rememberSaveable { mutableStateOf(false) }; var showMapPicker by rememberSaveable { mutableStateOf(false) }; var locationMessage by rememberSaveable { mutableStateOf("") }; val context = LocalContext.current
 val picker= rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){photo=it?.toString();error=""}
 val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants -> if (grants.values.any { it }) requestReportLocation(context) { loc -> latitude=loc.latitude; longitude=loc.longitude; gpsActive=true; locationSelected=true; locationMessage="Ubicación GPS actualizada" } else locationMessage="Permite el acceso a la ubicación para usar el GPS." }
 Column(Modifier.fillMaxSize().background(Color(0xFFF5F7FC))){
  Row(Modifier.fillMaxWidth().height(47.dp).background(Color.White).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){
   IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Volver",tint=RN)}
   Text("▧ VozBarrial",Modifier.weight(1f),color=RN,fontWeight=FontWeight.Bold,fontSize=13.sp);Text("Reportar Incidente",color=RN,fontWeight=FontWeight.Bold,fontSize=10.sp)
  }
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
   Column(verticalArrangement=Arrangement.spacedBy(3.dp)){
    Text("✿  Red de Vigilancia Activa",Modifier.background(Color(0xFFDDF8EF),RoundedCornerShape(30.dp)).padding(horizontal=9.dp,vertical=4.dp),color=RG,fontSize=10.sp,fontWeight=FontWeight.Bold)
    Text("Crear Reporte Ciudadano",color=RN,fontSize=19.sp,fontWeight=FontWeight.Bold)
    Text("Ayuda a mejorar la seguridad y convivencia de tu barrio.",color=RM,fontSize=11.sp)
    Text("● Paso 1: Categoría     Paso 2: Evidencia     Paso 3: Detalles",color=RM,fontSize=9.sp)
    LinearProgressIndicator(progress={.38f},modifier=Modifier.fillMaxWidth().height(4.dp),color=Color(0xFFFF8138),trackColor=RB)
   }
   Section("1","Selecciona la Categoría","Requerido"){
    Box {
     val selected = reportCategories.first { it.name == category }
     Column {
      Row(Modifier.fillMaxWidth().background(RB,RoundedCornerShape(10.dp)).clickable { menu = true }.padding(9.dp), verticalAlignment=Alignment.CenterVertically) {
       Box(Modifier.size(31.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE1E1)), contentAlignment=Alignment.Center) { Text(selected.icon, fontSize=16.sp) }
       Spacer(Modifier.width(9.dp))
       Column(Modifier.weight(1f)) { Row(verticalAlignment=Alignment.CenterVertically) { Text(selected.name,color=RN,fontSize=12.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.width(6.dp)); Text("Seleccionada",Modifier.background(Color(0xFFDDF8EF),RoundedCornerShape(20.dp)).padding(horizontal=5.dp,vertical=2.dp),color=RG,fontSize=8.sp,fontWeight=FontWeight.Bold) }; Text(selected.description,color=RM,fontSize=9.sp,maxLines=1) }
       Text("⌃",color=RN,fontSize=16.sp)
      }
      DropdownMenu(expanded=menu,onDismissRequest={menu=false},modifier=Modifier.width(300.dp).background(Color.White)) {
       Text("CATEGORÍAS DISPONIBLES",Modifier.fillMaxWidth().padding(horizontal=13.dp,vertical=8.dp),color=RN,fontSize=9.sp,fontWeight=FontWeight.Bold)
       reportCategories.forEach { option ->
        Row(Modifier.fillMaxWidth().background(if(option.name==category)Color(0xFFE8F1FF)else Color.White).clickable { category=option.name;menu=false }.padding(horizontal=11.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
         Box(Modifier.size(29.dp).clip(RoundedCornerShape(7.dp)).background(if(option.name=="Seguridad")Color(0xFFFFE1E1)else Color(0xFFDDEAFF)),contentAlignment=Alignment.Center){Text(option.icon,fontSize=15.sp)}
         Spacer(Modifier.width(9.dp))
         Column(Modifier.weight(1f)) { Text(option.name,color=RN,fontSize=11.sp,fontWeight=if(option.name==category)FontWeight.Bold else FontWeight.Medium,maxLines=1);Text(option.description,color=RM,fontSize=8.sp,maxLines=1) }
         if(option.name==category)Text("✓",color=RN,fontSize=15.sp,fontWeight=FontWeight.Bold)
        }
       }
      }
     }
    }
   }
   Section("2","Evidencia Fotográfica","Mín. 1 foto"){
    Column(Modifier.fillMaxWidth().background(RB,RoundedCornerShape(10.dp)).clickable{picker.launch("image/*")}.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)){
     Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFD8E5FF)),contentAlignment=Alignment.Center){Icon(Icons.Default.CameraAlt,null,tint=RN)}
     Text(if(photo==null)"Subir fotografía del incidente" else "Fotografía lista para adjuntar",color=RN,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
     Text(if(photo==null)"Captura directa con cámara o galería (JPG, PNG)" else "Toca para cambiar la fotografía seleccionada",color=RM,fontSize=10.sp,textAlign=TextAlign.Center)
     Text("♧ Almacenamiento seguro y cifrado",color=Color(0xFFE56722),fontSize=9.sp)
    }
    if(photo!=null)Row(Modifier.fillMaxWidth().background(Color(0xFFDDF8EF),RoundedCornerShape(8.dp)).padding(8.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CheckCircle,null,tint=RG);Spacer(Modifier.width(6.dp));Text("Evidencia adjunta",color=RG,fontSize=10.sp)}
   }
   Section("3","Ubicación del Incidente",if(gpsActive)"GPS Activo" else "Ubicación"){
    Column(verticalArrangement=Arrangement.spacedBy(7.dp)) {
     Box(Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2EEDD)).clickable { showMapPicker=true },contentAlignment=Alignment.Center) {
      Text("⌁    ⌁      ⌁\n   ⌁     ⌁    ⌁",color=Color(0xFF91AFC0),fontSize=26.sp,textAlign=TextAlign.Center)
      Text("Toca para elegir punto en el mapa",Modifier.align(Alignment.TopCenter).padding(top=7.dp).background(RN,RoundedCornerShape(20.dp)).padding(horizontal=9.dp,vertical=5.dp),color=Color.White,fontSize=9.sp)
      Icon(Icons.Default.LocationOn,null,tint=Color(0xFFFF7629),modifier=Modifier.align(Alignment.Center).size(30.dp))
      Text("Abrir mapa  ↗",Modifier.align(Alignment.BottomEnd).padding(7.dp).background(Color.White,RoundedCornerShape(20.dp)).padding(6.dp),color=RN,fontSize=9.sp,fontWeight=FontWeight.Bold)
     }
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
      OutlinedButton(onClick={showMapPicker=true},modifier=Modifier.weight(1f).height(40.dp),contentPadding=PaddingValues(4.dp),shape=RoundedCornerShape(8.dp)){Text("⌖  Elegir en mapa",fontSize=10.sp,color=RN)}
      Button(onClick={
       val fine=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
       val coarse=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
       if(fine||coarse) requestReportLocation(context){loc->latitude=loc.latitude;longitude=loc.longitude;gpsActive=true;locationSelected=true;locationMessage="Ubicación GPS actualizada"}
       else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))
      },modifier=Modifier.weight(1f).height(40.dp),contentPadding=PaddingValues(4.dp),shape=RoundedCornerShape(8.dp),colors=ButtonDefaults.buttonColors(containerColor=RN)){Text("◎  Usar mi GPS",fontSize=10.sp)}
     }
     Row(Modifier.fillMaxWidth().background(RB,RoundedCornerShape(8.dp)).padding(8.dp),verticalAlignment=Alignment.CenterVertically){
      Icon(Icons.Default.LocationOn,null,tint=RN,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp))
      Column(Modifier.weight(1f)){Text(if(gpsActive)"Ubicación GPS actual" else if(locationSelected)"Punto elegido en el mapa" else "Selecciona la ubicación del incidente",color=RN,fontSize=11.sp,fontWeight=FontWeight.Bold);Text("Lat: ${String.format(java.util.Locale.US,"%.5f",latitude)}, Lon: ${String.format(java.util.Locale.US,"%.5f",longitude)}",color=RM,fontSize=9.sp)}
      if(gpsActive)Icon(Icons.Default.CheckCircle,null,tint=RG,modifier=Modifier.size(17.dp))
     }
     if(locationMessage.isNotBlank()) Text(locationMessage,color=if(gpsActive)RG else Color(0xFFB42318),fontSize=9.sp)
    }
   }
   Section("4","Información Detallada",null){
    Text("Título del reporte",color=RN,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
    OutlinedTextField(title,{title=it.take(60);error=""},Modifier.fillMaxWidth(),placeholder={Text("Ej. Luminaria rota en esquina de parque",fontSize=10.sp)},supportingText={Text("${title.length}/60",Modifier.fillMaxWidth(),textAlign=TextAlign.End)},singleLine=true,shape=RoundedCornerShape(9.dp),colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=Color.White,focusedContainerColor=Color.White))
    Text("Descripción de lo sucedido",color=RN,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
    OutlinedTextField(description,{description=it.take(280);error=""},Modifier.fillMaxWidth().height(104.dp),placeholder={Text("Describe detalles relevantes: hora aproximada, puntos de referencia clave o si hay personas vulnerables en riesgo...",fontSize=10.sp,lineHeight=13.sp)},supportingText={Text("${description.length}/280",Modifier.fillMaxWidth(),textAlign=TextAlign.End)},shape=RoundedCornerShape(9.dp),colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=Color.White,focusedContainerColor=Color.White))
   }
   if(error.isNotBlank())Text(error,color=Color(0xFFB42318),fontSize=11.sp)
  }
  Button(onClick={error=when{title.isBlank()->"Escribe un título para el reporte.";description.isBlank()->"Describe brevemente lo sucedido.";!locationSelected->"Usa el GPS o elige un punto en el mapa.";photo==null->"Adjunta al menos una fotografía como evidencia.";else->""};if(error.isBlank())onPublish(title.trim(),description.trim(),category,latitude,longitude)},Modifier.fillMaxWidth().padding(horizontal=10.dp,vertical=7.dp).height(49.dp),shape=RoundedCornerShape(9.dp),colors=ButtonDefaults.buttonColors(containerColor=RN)){Icon(Icons.Default.Send,null);Spacer(Modifier.width(7.dp));Text("Publicar Reporte Ciudadano",fontWeight=FontWeight.Bold,fontSize=12.sp)}
  if(showMapPicker) ReportMapPicker(latitude,longitude,onConfirm={lat,lon->latitude=lat;longitude=lon;gpsActive=false;locationSelected=true;locationMessage="Punto seleccionado en el mapa";showMapPicker=false},onDismiss={showMapPicker=false})
 }
}

@Composable
private fun ReportMapPicker(initialLat: Double, initialLon: Double, onConfirm: (Double,Double)->Unit, onDismiss: ()->Unit) {
    var selectedLat by remember(initialLat) { mutableDoubleStateOf(initialLat) }
    var selectedLon by remember(initialLon) { mutableDoubleStateOf(initialLon) }
    var hasMoved by remember { mutableStateOf(false) }
    val html = remember(initialLat,initialLon) { """
        <!DOCTYPE html><html><head><meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
        <style>html,body,#map{height:100%;margin:0} .leaflet-control-attribution{font-size:9px}</style>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script></head>
        <body><div id="map"></div><script>
        const map=L.map('map').setView([$initialLat,$initialLon],16);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'© OpenStreetMap'}).addTo(map);
        const marker=L.marker([$initialLat,$initialLon],{draggable:true}).addTo(map);
        function picked(){const p=marker.getLatLng();window.location.href='voz://pick?lat='+p.lat+'&lon='+p.lng;}
        map.on('click',e=>{marker.setLatLng(e.latlng);picked();}); marker.on('dragend',picked);
        </script></body></html>
    """.trimIndent() }
    Dialog(onDismissRequest=onDismiss) {
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Color.White) {
            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("Elige la ubicación",color=RN,fontSize=16.sp,fontWeight=FontWeight.Bold)
                Text("Toca el mapa o arrastra el pin hasta el lugar del incidente.",color=RM,fontSize=11.sp)
                AndroidView(factory={ctx->WebView(ctx).apply {
                    settings.javaScriptEnabled=true
                    webViewClient=object:WebViewClient(){
                        override fun shouldOverrideUrlLoading(view:WebView,request:android.webkit.WebResourceRequest):Boolean {
                            if(request.isForMainFrame && request.url.scheme=="voz") {
                                val lat=request.url.getQueryParameter("lat")?.toDoubleOrNull()
                                val lon=request.url.getQueryParameter("lon")?.toDoubleOrNull()
                                if(lat!=null&&lon!=null){selectedLat=lat;selectedLon=lon;hasMoved=true}
                                return true
                            }
                            return false
                        }
                    }
                    loadDataWithBaseURL("https://picker.local/",html,"text/html","UTF-8",null)
                }},modifier=Modifier.fillMaxWidth().height(390.dp).clip(RoundedCornerShape(12.dp)))
                Text("Lat: ${String.format(java.util.Locale.US,"%.5f",selectedLat)} · Lon: ${String.format(java.util.Locale.US,"%.5f",selectedLon)}",color=RM,fontSize=10.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick=onDismiss,modifier=Modifier.weight(1f)){Text("Cancelar")}
                    Button(onClick={onConfirm(selectedLat,selectedLon)},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=RN)){Text(if(hasMoved)"Usar este punto" else "Confirmar punto")}
                }
            }
        }
    }
}

private fun requestReportLocation(context: Context, onLocation: (Location)->Unit) {
    val manager=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val fine=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    val providers=manager.getProviders(true).filter { it!="gps" || fine }
    val cached=providers.mapNotNull { provider -> try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } }.maxByOrNull { it.time }
    if(cached!=null && System.currentTimeMillis()-cached.time<30_000L){onLocation(cached);return}
    var delivered=false
    val listener=object:LocationListener{
        override fun onLocationChanged(location:Location){
            if(!delivered){delivered=true;try{manager.removeUpdates(this)}catch(_:SecurityException){};onLocation(location)}
        }
        @Deprecated("Deprecated by Android")
        override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?){}
        override fun onProviderEnabled(provider:String){}
        override fun onProviderDisabled(provider:String){}
    }
    providers.forEach { provider -> try { manager.requestLocationUpdates(provider,1000L,0f,listener,Looper.getMainLooper()) } catch (_:SecurityException) {} }
    android.os.Handler(Looper.getMainLooper()).postDelayed({if(!delivered)try{manager.removeUpdates(listener)}catch(_:SecurityException){}},12_000L)
}

@Composable private fun Section(number:String,title:String,badge:String?,content:@Composable ColumnScope.()->Unit){
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(10.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(20.dp).clip(CircleShape).background(RN),contentAlignment=Alignment.Center){Text(number,color=Color.White,fontSize=9.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(7.dp));Text(title,Modifier.weight(1f),color=RN,fontSize=12.sp,fontWeight=FontWeight.Bold);if(badge!=null)Text(badge,Modifier.background(if(badge.contains("GPS"))Color(0xFFFFE4D8)else Color(0xFFDDF8EF),RoundedCornerShape(30.dp)).padding(horizontal=6.dp,vertical=3.dp),color=if(badge.contains("GPS"))Color(0xFFE56722)else RG,fontSize=9.sp,fontWeight=FontWeight.Bold)}
  content()
 }
}
