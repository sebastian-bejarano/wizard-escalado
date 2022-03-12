const observer = new MutationObserver(function(mutations_list) {
    mutations_list.forEach(function(mutation) {
		mutation.addedNodes.forEach(function(added_node) {
            if(added_node.id == 'mi-item-link-dialog') {
        		alert('el dialogo se abrió');
    			observer.disconnect();
    		}
        });
    });
});
observer.observe(document.querySelector("body"), {childList: true });