package com.vozbarrial.features.dashboard

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vozbarrial.ui.theme.*
import com.vozbarrial.features.profile.EditProfileViewModel

@Composable
fun EditProfile(name:String,phone:String,photo:String?,onCancel:()->Unit,onSave:(String,String,String?)->Unit) {
 val context= LocalContext.current
 val editViewModel: EditProfileViewModel = viewModel()
 val state by editViewModel.uiState.collectAsStateWithLifecycle()
 LaunchedEffect(name, phone, photo) { editViewModel.initialize(name, phone, photo) }
 val fullName = state.name
 val phoneNumber = state.phone
 val photoUri = state.photoUri
 val error = state.error.orEmpty()
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
  if(uri!=null) {
   val mime=context.contentResolver.getType(uri)
   var size=0L
   context.contentResolver.query(uri,arrayOf(OpenableColumns.SIZE),null,null,null)?.use { c->if(c.moveToFirst())size=c.getLong(0) }
   when {
    mime !in listOf("image/jpeg","image/png") -> editViewModel.showError("Selecciona una foto JPG o PNG.")
    size>5*1024*1024 -> editViewModel.showError("La foto debe pesar máximo 5 MB.")
    else -> {
     try { context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch(_:SecurityException) {}
     editViewModel.setPhoto(uri.toString())
    }
   }
  }
 }
 val bitmap=remember(photoUri){runCatching { photoUri?.let { context.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream)?.asImageBitmap() } }.getOrNull()}
 Column(Modifier.fillMaxSize().background(VozBackground)) {
  Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=12.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
   Text("‹",Modifier.clickable(onClick=onCancel).padding(horizontal=8.dp),fontSize=25.sp,color=VozNavy)
   Surface(Modifier.size(25.dp),shape=CircleShape,color=VozNavy){Box(contentAlignment=Alignment.Center){Text("V",color=Color.White,fontWeight=FontWeight.Bold,fontSize=12.sp)}}
   Text("Editar perfil",Modifier.weight(1f).padding(start=8.dp),fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=VozNavy)
  }
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=14.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp)) {
    Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
     Box(Modifier.size(104.dp),contentAlignment=Alignment.BottomEnd) {
      if(bitmap!=null) Image(bitmap,contentDescription="Foto de perfil",modifier=Modifier.fillMaxSize().clip(CircleShape),contentScale=ContentScale.Crop)
      else Surface(Modifier.fillMaxSize(),shape=CircleShape,color=VozBackground,shadowElevation=2.dp) {
       Box(contentAlignment=Alignment.Center){Text(fullName,Modifier.padding(12.dp),textAlign=TextAlign.Center,color=VozNavy,fontSize=14.sp,fontWeight=FontWeight.Medium)}
      }
      Surface(Modifier.size(34.dp).clickable{picker.launch(arrayOf("image/jpeg","image/png"))},shape=CircleShape,color=VozNavy,shadowElevation=3.dp) {
       Box(contentAlignment=Alignment.Center){Text("📷",fontSize=16.sp)}
      }
     }
     Spacer(Modifier.height(7.dp))
     Text(fullName,color=VozNavy,fontSize=15.sp,fontWeight=FontWeight.Bold)
     Text("Toca el ícono de cámara para actualizar tu foto (JPG o PNG, máx. 5MB)",Modifier.padding(top=2.dp),textAlign=TextAlign.Center,color=VozNavy,fontSize=10.sp,lineHeight=13.sp)
    }
   }
   Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp)) {
    Column(Modifier.fillMaxWidth().padding(13.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
     Text("♙  Datos Personales",color=VozNavy,fontSize=15.sp,fontWeight=FontWeight.Bold)
     Text("NOMBRE COMPLETO",color=VozNavy,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
     OutlinedTextField(value=fullName,onValueChange=editViewModel::updateName,modifier=Modifier.fillMaxWidth().height(54.dp),singleLine=true,placeholder={Text("Nombre y apellidos",fontSize=12.sp)},leadingIcon={Text("♙",color=Color(0xFF68788B))},shape=RoundedCornerShape(12.dp),colors=OutlinedTextFieldDefaults.colors(focusedContainerColor=VozBlueSurface,unfocusedContainerColor=VozBlueSurface,focusedBorderColor=Color.Transparent,unfocusedBorderColor=Color.Transparent))
     Text("TELÉFONO DE ALERTAS CIUDADANAS",color=VozNavy,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
     OutlinedTextField(value=phoneNumber,onValueChange={value->editViewModel.updatePhone(value.filter{ch->ch.isDigit()||ch=='+'||ch==' '}.take(18))},modifier=Modifier.fillMaxWidth().height(54.dp),singleLine=true,placeholder={Text("+57 312 456 7890",fontSize=12.sp)},leadingIcon={Text("☎",color=Color(0xFF68788B))},shape=RoundedCornerShape(12.dp),colors=OutlinedTextFieldDefaults.colors(focusedContainerColor=VozBlueSurface,unfocusedContainerColor=VozBlueSurface,focusedBorderColor=Color.Transparent,unfocusedBorderColor=Color.Transparent))
    }
   }
   Button(onClick={editViewModel.save(onSave)},modifier=Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(11.dp),colors=ButtonDefaults.buttonColors(containerColor=VozNavy),elevation=ButtonDefaults.buttonElevation(defaultElevation=3.dp)){Text("▣  Guardar Cambios",color=Color.White,fontWeight=FontWeight.Bold,fontSize=14.sp)}
   Surface(Modifier.fillMaxWidth().height(42.dp).clickable(onClick=onCancel),shape=RoundedCornerShape(11.dp),color=VozBlueSurface){Box(contentAlignment=Alignment.Center){Text("Cancelar Cambios",color=VozNavy,fontSize=14.sp)}}
   Spacer(Modifier.height(6.dp))
  }
 }
 if(error.isNotBlank())AlertDialog(onDismissRequest=editViewModel::clearError,title={Text("Revisa los datos",color=VozNavy)},text={Text(error)},confirmButton={TextButton(onClick=editViewModel::clearError){Text("Entendido",color=VozGreen)}})
}
