//Esta función va a observar en el momento en el que se haga click al botón "ESCALAR A JIRA SOFTWARE" para verificar la cajita y jugar con ella
const click_function = () => {
    observer.observe(document.querySelector("#jira"), { subtree: false, childList: true });
}
//Esta función va a ser el primer callback que tenga la página después de esperar unos segundos a que cargue
const callback = () => {
    //Se busca el elemento botón en el dom para ver que esté
    let boton = document.querySelector("#mi-item-link");
    if(boton){
        //Le añadimos una función click;
        boton.onclick = click_function;
    }
}
//callback para el boton de submit

const submitBtn_callback = () => {
//Con ayuda de jQuery vamos a ajustar el CSS de la ventana
    setTimeout(function(){
        window_callback();
    },50);
}

//Callback para el boton de cerrar
const closeBtn_callback = () => {
    setTimeout(function(){
        //Adjuntamos un método al botón cerrar para que remueva el diálogo y sus child nodes además del fondo negro
        $("#mi-item-link-dialog").remove();
        $(".aui-blanket").remove();
    },50);
}
//Esta función va a verificar siempre que la ventana esté en el tamaño correcto para el plugin
const window_callback = () => {
    console.log("Acá se configura el timer");
    //Colocamos un timer de 50ms para esperar a que aparezca la ventana
    setTimeout(function(){
        //agregamos el callback al botón cerrar
        document.querySelector("#closeBtn").onclick = closeBtn_callback;
        //Hacemos que el botón submit llame al callback de la ventana
        const submitBtn = document.querySelector("#submitBtn");
        submitBtn.onclick = submitBtn_callback;
    },300);
}
//Esta es la función que se va a llamar la primera vez que se abra la ventana al darle click
const first_window_callback = () =>{
    //Con ayuda de jQuery ajustamos el css de la ventana
    //Adjuntamos un método al botón cerrar para que remueva el diálogo y sus child nodes además del fondo negro
    document.querySelector("#closeBtn").onclick = closeBtn_callback;
    //Hacemos que el botón submit llame al callback de la ventana
    const submitBtn = document.querySelector("#submitBtn");
    submitBtn.onclick = submitBtn_callback;
}
const observer = new MutationObserver(function(mutations_list) {
   	mutations_list.forEach(function(mutation) {
   		mutation.addedNodes.forEach(function(added_node) {
   			if(added_node.id == 'mi-item-link-dialog') {
   				console.log('mi-item-link-dialog has been added');
   				$("#mi-item-link-dialog").ready(()=>{
                    setTimeout(first_window_callback,300);
   				});
   				observer.disconnect();
   			}
   		});
 	});
});

window.document.addEventListener('DOMContentLoaded', (event) => {
    //Se agrega timeout con el fin de que se cargue TODO el DOM
    setTimeout(callback,3000);
});