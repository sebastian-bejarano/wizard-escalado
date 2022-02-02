const click_function = () => {
    observer.observe(document.querySelector("#jira"), { subtree: false, childList: true });
}
const callback = () => {
    //Se busca el elemento botón en el dom para ver que esté
    let boton = document.querySelector("#mi-item-link");
    //Le añadimos una función click;
    boton.onclick = click_function;
}

const window_callback = () => {
    setTimeout(function(){
        $("#mi-item-link-dialog").css(
            {
                "width":"50%",
                "transform":"translate(-23%, -50%)"
            }
        );
    },50);
}

const first_window_callback = () =>{
    $("#mi-item-link-dialog").css(
        {
            "width":"50%",
            "transform":"translate(-23%, -50%)"
        }
    );
    $(".groupInput").css(
        {
            "border":"1px solid black",
            "width":"20%",
            "margin-left":"auto",
            "margin-right":"auto"
        }
    );
    $("#closeBtn").click(() => {
        $("#mi-item-link-dialog").remove();
        $(".aui-blanket").remove();
    });
    const submitBtn = document.querySelector("#submitBtn");
    submitBtn.onclick = window_callback;
}
const observer = new MutationObserver(function(mutations_list) {
   	mutations_list.forEach(function(mutation) {
   		mutation.addedNodes.forEach(function(added_node) {
   			if(added_node.id == 'mi-item-link-dialog') {
   				console.log('mi-item-link-dialog has been added');
   				$("#mi-item-link-dialog").ready(()=>{
                    setTimeout(first_window_callback,100);
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