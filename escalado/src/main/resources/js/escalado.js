//Esta función va a observar en el momento en el que se haga click al botón "ESCALAR A JIRA SOFTWARE" para verificar la cajita y jugar con ella
const click_function = () => {
    observer.observe(document.querySelector("#jira"), { subtree: false, childList: true });
}
//Esta función va a ser el primer callback que tenga la página después de esperar unos segundos a que cargue
const callback = () => {
    //Se busca el elemento botón en el dom para ver que esté
    let boton = document.querySelector("#mi-item-link");
    //Le añadimos una función click;
    boton.onclick = click_function;
}

//Esta función va a verificar siempre que la ventana esté en el tamaño correcto para el plugin
const window_callback = () => {
    //Colocamos un timer de 50ms para esperar a que aparezca la ventana
    setTimeout(function(){
        //Con ayuda de jQuery vamos a ajustar el CSS de la ventana
        $("#mi-item-link-dialog").css(
            {
                "width":"50%",
                "transform":"translate(-19%, -50%)"
            }
        );
    },25);
    //Adjuntamos un método al botón cerrar
    $("#closeBtn").click(() => {
        $("#mi-item-link-dialog").remove();
        $(".aui-blanket").remove();
    });
    //Hacemos que el botón submit llame al callback de la ventana
    const submitBtn = document.querySelector("#submitBtn");
    console.log(submitBtn);
    submitBtn.onclick = window_callback;
}
//Esta es la función que se va a llamar la primera vez que se abra la ventana al darle click
const first_window_callback = () =>{
    //Con ayuda de jQuery ajustamos el css de la ventana
    $("#mi-item-link-dialog").css(
        {
            "width":"18%",
            "transform":"translate(20%, -50%)"
        }
    );
    //Ahora ajustamos el CSS de los labels para que queden centrados
    $(".groupInput").css(
        {
            "width":"30%",
            "margin-left":"auto",
            "margin-right":"auto"
        }
    );
    //Adjuntamos un método al botón cerrar para que remueva el diálogo y sus child nodes además del fondo negro
    $("#closeBtn").click(() => {
        $("#mi-item-link-dialog").remove();
        $(".aui-blanket").remove();
    });
    //Hacemos que el botón submit llame al callback de la ventana
    const submitBtn = document.querySelector("#submitBtn");
    submitBtn.onclick = window_callback;
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