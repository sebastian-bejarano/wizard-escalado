setInterval(() =>{
    let cambiarNombreCheckBox = document.querySelector("#cambiarNombre");
    if(cambiarNombreCheckBox){
        cambiarNombreCheckBox.onclick = changeFunction;
        document.querySelector("#nombreNuevo").addEventListener('input', updateValue);
    }
},50);


const changeFunction = () => {
    if(document.querySelector("#cambiarNombre").checked){
        document.querySelector("#submitBtn").disabled = true;
        document.querySelector("#nombreNuevo").removeAttribute("readonly");
    }
    if(!document.querySelector("#cambiarNombre").checked){
        document.querySelector("#nombreNuevo").setAttribute('readonly', true);
        document.querySelector("#nombreNuevo").value = "";
        document.querySelector("#nombreNuevo").removeAttribute("disabled");
    }
    console.log(document.querySelector("#nombreNuevo"));
};

function updateValue(e) {
  let btnEnviar = document.querySelector("#submitBtn");
  if(document.querySelector("#cambiarNombre").checked){
    if(e.srcElement.value.length>0){
      btnEnviar.disabled = false;
    }
    else{
      btnEnviar.disabled = true;
    }
  }
  else{
    btnEnviar.disabled = false;
  }
}