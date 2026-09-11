package com.coatlab.app

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { CoatLabApp() } }
    }
}

private enum class Section { HOME, PAINTLAB, DIAGNOSE, QC, HEALTH }

@Composable
private fun CoatLabApp() {
    var section by remember { mutableStateOf(Section.HOME) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Section.HOME to "خانه",
                    Section.PAINTLAB to "PaintLab",
                    Section.DIAGNOSE to "تشخیص",
                    Section.QC to "QC"
                ).forEach { item ->
                    NavigationBarItem(
                        selected = section == item.first,
                        onClick = { section = item.first },
                        icon = { Text("•") },
                        label = { Text(item.second) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (section) {
                Section.HOME -> HomeScreen(
                    { section = Section.PAINTLAB },
                    { section = Section.DIAGNOSE },
                    { section = Section.QC },
                    { section = Section.HEALTH }
                )
                Section.PAINTLAB -> PaintLabScreen()
                Section.DIAGNOSE -> DiagnoseScreen()
                Section.QC -> QcScreen()
                Section.HEALTH -> HealthScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(openPaint:()->Unit, openDiag:()->Unit, openQc:()->Unit, openHealth:()->Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CoatLab", style = MaterialTheme.typography.headlineLarge)
        Text("Formulate • Test • Diagnose")
        HomeCard("PaintLab", "محاسبه فرمول، هزینه و درصد وزنی", openPaint)
        HomeCard("AI Troubleshooter", "ثبت عکس و اطلاعات فرآیندی عیب", openDiag)
        HomeCard("QC Laboratory", "کنترل نتایج آزمایشگاهی", openQc)
        HomeCard("System Health", "وضعیت نسخه داخلی و AI", openHealth)
    }
}

@Composable
private fun HomeCard(title:String, subtitle:String, action:()->Unit) {
    ElevatedCard(onClick=action, modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(title, style=MaterialTheme.typography.titleLarge)
            Text(subtitle)
        }
    }
}

@Composable
private fun PaintLabScreen() {
    var resinWeight by remember { mutableStateOf("32.5") }
    var resinPrice by remember { mutableStateOf("4.2") }
    var pigmentWeight by remember { mutableStateOf("18") }
    var pigmentPrice by remember { mutableStateOf("3.6") }
    val rw=resinWeight.toDoubleOrNull() ?: 0.0
    val rp=resinPrice.toDoubleOrNull() ?: 0.0
    val pw=pigmentWeight.toDoubleOrNull() ?: 0.0
    val pp=pigmentPrice.toDoubleOrNull() ?: 0.0
    val totalWeight=rw+pw
    val totalCost=rw*rp+pw*pp

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        Text("PaintLab", style=MaterialTheme.typography.headlineMedium)
        MaterialEditor("Resin",resinWeight,{resinWeight=it},resinPrice,{resinPrice=it})
        MaterialEditor("Pigment",pigmentWeight,{pigmentWeight=it},pigmentPrice,{pigmentPrice=it})
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text("خلاصه فرمول", style=MaterialTheme.typography.titleMedium)
                Text("وزن کل: " + "%.2f".format(totalWeight) + " kg")
                Text("هزینه کل: " + "%.2f".format(totalCost))
                Text("هزینه/kg: " + if(totalWeight>0) "%.2f".format(totalCost/totalWeight) else "0.00")
                Text("Resin wt%: " + if(totalWeight>0) "%.1f".format(rw/totalWeight*100) else "0")
                Text("Pigment wt%: " + if(totalWeight>0) "%.1f".format(pw/totalWeight*100) else "0")
            }
        }
    }
}

@Composable
private fun MaterialEditor(name:String,weight:String,onWeight:(String)->Unit,price:String,onPrice:(String)->Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(name, style=MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(weight,onWeight,label={Text("Weight kg")},modifier=Modifier.weight(1f))
                OutlinedTextField(price,onPrice,label={Text("Price/kg")},modifier=Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QcScreen() {
    var viscosity by remember { mutableStateOf("82") }
    var gloss by remember { mutableStateOf("86") }
    var dft by remember { mutableStateOf("72") }

    fun status(value:String,min:Double,max:Double):String {
        val v=value.toDoubleOrNull() ?: return "WARNING"
        return if(v in min..max) "PASS" else "FAIL"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        Text("QC Laboratory",style=MaterialTheme.typography.headlineMedium)
        QcItem("Viscosity",viscosity,{viscosity=it},"KU","78–86",status(viscosity,78.0,86.0))
        QcItem("Gloss 60°",gloss,{gloss=it},"GU","80–100",status(gloss,80.0,100.0))
        QcItem("DFT",dft,{dft=it},"µm","60–80",status(dft,60.0,80.0))
    }
}

@Composable
private fun QcItem(name:String,value:String,onValue:(String)->Unit,unit:String,limits:String,status:String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(name,style=MaterialTheme.typography.titleMedium)
                Text(status)
            }
            OutlinedTextField(value,onValue,label={Text(unit)},modifier=Modifier.fillMaxWidth())
            Text("Limit: " + limits,style=MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DiagnoseScreen() {
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var temp by remember { mutableStateOf("25") }
    var rh by remember { mutableStateOf("55") }
    var dft by remember { mutableStateOf("60") }
    var message by remember { mutableStateOf("مدل AI واقعی هنوز به این Internal Build اضافه نشده است.") }

    val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        photo=bitmap
        if(bitmap!=null) message="تصویر ثبت شد. Engineering pre-check آماده است."
    }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if(granted) camera.launch(null) else message="اجازه دوربین داده نشد."
    }

    val findings=mutableListOf<String>()
    val t=temp.toDoubleOrNull()
    val humidity=rh.toDoubleOrNull()
    val thickness=dft.toDoubleOrNull()
    if(humidity!=null && humidity>85) findings.add("RH بالا: ریسک condensation / adhesion issue")
    if(t!=null && t>35) findings.add("دمای بالا: ریسک dry spray / solvent loss")
    if(thickness!=null && thickness>150) findings.add("DFT بالا: ریسک solvent popping / sagging")
    if(findings.isEmpty()) findings.add("از ورودی‌های فعلی هشدار فرآیندی واضحی دیده نشد.")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        Text("AI Troubleshooter",style=MaterialTheme.typography.headlineMedium)
        Button(onClick={permission.launch(Manifest.permission.CAMERA)},modifier=Modifier.fillMaxWidth()) { Text("گرفتن عکس") }
        photo?.let { Image(it.asImageBitmap(),contentDescription="Defect photo",modifier=Modifier.fillMaxWidth().height(220.dp)) }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(temp,{temp=it},label={Text("°C")},modifier=Modifier.weight(1f))
            OutlinedTextField(rh,{rh=it},label={Text("RH%")},modifier=Modifier.weight(1f))
            OutlinedTextField(dft,{dft=it},label={Text("DFT µm")},modifier=Modifier.weight(1f))
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("Engineering Pre-check",style=MaterialTheme.typography.titleMedium)
                findings.forEach { Text("• " + it) }
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("Visual AI",style=MaterialTheme.typography.titleMedium)
                Text(message)
                Text("هیچ درصد یا تشخیص ساختگی نمایش داده نمی‌شود.",style=MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun HealthScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        Text("System Health",style=MaterialTheme.typography.headlineMedium)
        HealthItem("App","Ready","CoatLab 1.5.2 Internal")
        HealthItem("Camera","Ready","Runtime permission + preview capture")
        HealthItem("PaintLab","Ready","Formula costing")
        HealthItem("QC","Ready","PASS/FAIL limits")
        HealthItem("Engineering Pre-check","Ready","Temperature / RH / DFT")
        HealthItem("Visual AI","Not connected","Awaiting trained, calibrated coating-defect model.")
    }
}

@Composable
private fun HealthItem(name:String,status:String,detail:String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(name,style=MaterialTheme.typography.titleMedium)
                Text(status)
            }
            Text(detail,style=MaterialTheme.typography.bodySmall)
        }
    }
}
