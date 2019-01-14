AJS.bind("init.rte",function (){
	prepareEditor();
        panel = false;
});//init
    
jQuery(function () {
    jQuery("#insert-menu-options").find("#macro-insert-list").find(".macro-mscgen").click(function(){
        AJS.MacroBrowser.setMacroJsOverride("mscgen", {opener: function(macro) {      
            var currentNode=AJS.$(AJS.Rte.getEditor().selection.getNode());
            showEditor(currentNode);
            panel = true;
        }});
    });
    
    var editMscgen=false;
    jQuery('body').bind('DOMNodeInserted',function(e){            
        if(e.target.className == "aui-property-panel"){               
            if (jQuery.trim(jQuery('iframe').contents().find('body').find('table').attr('data-macro-name')) === "mscgen") {
                editMscgen=true;
            }
            if(editMscgen){
                jQuery(e.target).remove();
                editMscgen=false;
            }
        }
    });
});

jQuery(document).on('keyup',"#mscgen-textarea", function(e){
    if(jQuery('.ace_text-layer').text().length === 0)
        addPlaceholder();
    else if(jQuery(editor.renderer.scroller).find(editor.renderer.emptyMessageNode).length == 1){
        editor.renderer.scroller.removeChild(editor.renderer.emptyMessageNode);
        editor.renderer.emptyMessageNode = null;
    }
});

function addPlaceholder() {
        node = editor.renderer.emptyMessageNode;     
        node = editor.renderer.emptyMessageNode = document.createElement("div");
        node.textContent = "# AUTO Generated comment"+"\n"+"msc{"+"\n\n}"
        node.className = "ace_invisible ace_emptyMessage"
        node.style.padding = "0 9px"
        editor.renderer.scroller.appendChild(node);    
}
    
function prepareContent(){
	//content of dialog body; 
	var content='<form class="aui" id="mscgen-form" >'+
	    '<div id="mscgen-code-image-div" >'+
	    '<pre class="textarea" id="mscgen-textarea"></pre>'+
	    '<div id="mscgen-image-div">'+
	       '<img id="mscgen-image" alt="addteq" src="'+AJS.contextPath()+'/download/resources/com.addteq.confluence.plugin.mscgen.mscgen:confluence-mscgen-editor-resources/confluence-mscgen-images/mscgen-logo2.png">'+
	    '</div>'+
	    '</div>'+
	'</form>';
	return content;	
}

function showunlicensed(){
	var unlicenseddialog = new AJS.Dialog({
	    width: 450, 
	    height: 300,
	    id: "unlicensed-dialog", 
	    closeOnOutsideClick: false
	});
	var errorcontent = '<div class="aui-message error">'+
    	'<p class="title">'+
    	'<span class="aui-icon icon-error"></span>'+
    	'Addteq-Mscgen License is not valid. Please contact with your Confluence admin.</p>'+
    	'<p>If you have admin access, you can update the license <a id="a" href="'+AJS.contextPath()+'/plugins/servlet/upm">here</a>.</p></div>';
    	
	unlicenseddialog.addHeader("License Error");
	unlicenseddialog.addPanel("contentPanel", errorcontent, "panel-body");
	unlicenseddialog.addLink("Close", 
    function (unlicenseddialog) {
    	unlicenseddialog.remove();
        }, 
    "aui-button-link", "#");
	//when user clicks the link
	AJS.$('#a').click(function(){
		 window.location.href = '/plugins/servlet/upm';
	});
	//show image-display-dialog
	unlicenseddialog.show();
}

function showEditor(tgtpre){
	if(AJS.$("#mscgen-dialog").size()!=0){
		return;
	}
	jQuery.ajax({
		   url:AJS.contextPath()+"/secure/plugins/mscgen/checkLicense.action", 
		   type: "GET",
		   dataType: "text",
		   success: function(data) {
			  // console.log("Success in showeditor");
			   var index = data.indexOf("UNLICENSED");
			   if(index==-1){
				   //plugin is licensed
					var destpre=tgtpre;
					var dialog = new AJS.Dialog({
					    width: AJS.$(window).width(), 
					    height: AJS.$(window).height(), 
					    id: "mscgen-dialog", 
					    closeOnOutsideClick: false
					});
					
					//add dialog head + body
					dialog.addHeader("Insert Mscgen Code");
					dialog.addPanel("contentPanel", prepareContent(), "panel-body");
					
					addInsertButton(dialog,destpre);
					addPreviewButton(dialog);
					addCancelLink(dialog);
					
					
					//a9block
					editor = ace.edit("mscgen-textarea");
				    editor.setTheme("ace/theme/chrome");
				    editor.getSession().setMode("ace/mode/mscgen");
				    
					dialog.show();
					if(jQuery.trim(AJS.$(destpre).text())===""){
                                                addPlaceholder();
					}else{
						editor.setValue(AJS.$(destpre).text());
						AJS.$('#mscgen-dialog .dialog-button-panel button')[1].click();
					}
					dialogMarkup();
					addFullScreenImageView();
			   }
			   else{
				   //The plugin is unlicensed
				   showunlicensed();
				   return;
			   }
		   },
		   error: function(data) {
		   // console.log("There was an error with the request");
		   }
		}).done(function( data ) {
           //console.log("DONE");
    });

}
function dialogMarkup(){
	//buttons styles
	AJS.$('.dialog-button-panel button').css('width','10%').css('margin-right','3%');		
	AJS.$('.dialog-button-panel a').css('width','10%').css('margin-right','3%').css('text-align','center');		
	
	//close-x-icon styles
	AJS.$("#mscgen-dialog .dialog-title").append('<span id="close-x-icon" class="aui-icon aui-icon-small aui-iconfont-close-dialog"></span>');
	AJS.$('#mscgen-dialog #close-x-icon').click(function(){
			showConfirmation();
			//console.log("show confirm");
	    });

	AJS.$('.ace_print-margin').hide();
}

function addFullScreenImageView(){
	//initial image
        AJS.$('#mscgen-image').css('position','relative').css('top','14%');
	AJS.$('#mscgen-image').click(function(){
		//don't show placeholder image...
		if (AJS.$('#mscgen-image').attr("alt") == "addteq"){
		return false;
		}
		var image_display_dialog = new AJS.Dialog({
	    width: AJS.$(window).width(), 
	    height: AJS.$(window).height(), 
	    id: "image-display-dialog", 
	    closeOnOutsideClick: false
	});
		//image_display_dialog head + body
		image_display_dialog.addHeader("Image Display");
		image_display_dialog.addPanel("contentPanel", AJS.$('#mscgen-image').clone(), "panel-body");

		//add the back link
		image_display_dialog.addLink(
	    "Back", 
	    function (image_display_dialog) {
	    	image_display_dialog.remove();
	        }, 
	    "aui-button-link", "#");
		//show image-display-dialog
		image_display_dialog.show();
	});
}

function replaceCode(dp){
//	console.log("replacecode:"+AJS.$('.aui-message.error').size());
	if(AJS.$('.aui-message.error').size()==0){
		dp.text(editor.getValue());
		AJS.$('#mscgen-dialog').remove();
        AJS.$(".aui-blanket").remove(); 
	}
}

function addInsertButton(dialog,dp){
	dialog.addButton("Save", function (dialog) {
            if (panel)
                tinymce.confluence.macrobrowser.macroBrowserComplete({name: "mscgen", "bodyHtml": editor.getValue(), "params": {}});        
            
                panel= false;
		mscgenPreview(function(){
			replaceCode(dp);
		});
		
	});
}

function addPreviewButton(dialog){
	dialog.addButton("Preview", function (dialog) {
		mscgenPreview();
	});
}

function addCancelLink(dialog){
	dialog.addLink("Cancel",  function (dialog) {
		        showConfirmation();
		   		}, 
		    "aui-button-link", "#");
}
//this function is for showing confirmation dialog
function showConfirmation(){
	//confirmation
    var confirmDialog = new AJS.Dialog({
	    width: 400, 
	    height: 300, 
	    id: "confirm-dialog", 
	    closeOnOutsideClick: false
	});
	confirmDialog.addHeader("Confirmation");
	confirmDialog.addPanel("p", "<p>Are you sure you want to quit?</p>", "panel-body");
	confirmDialog.addButton("Quit", function (dialog) {
		AJS.$('#confirm-overlay').remove();
		AJS.$('#confirm-dialog').remove();
		AJS.$('#mscgen-dialog').remove();
        AJS.$(".aui-blanket").remove();  
	    });
    confirmDialog.addLink("Cancel", function (dialog) {
    AJS.$('#confirm-overlay').remove();
	AJS.$('#confirm-dialog').remove();
	    });
	confirmDialog.show();
	var confirmzindex=AJS.$('#confirm-dialog').css('z-index');
	AJS.$('#confirm-dialog').before('<div id="confirm-overlay" class="aui-blanket" style="z-index:'+confirmzindex+'"></div>');		 
}

/*
 * This will run when clicked on ConfluenceTable IN excellentable macro body.
 * change data-macro-name to switch target macro.
 */
function clickedFunction(){
	var currentNode=AJS.$(AJS.Rte.getEditor().selection.getNode());
	if(currentNode.parents('table[data-macro-name="mscgen"]').size()!=0){
		showEditor(currentNode);
	}else{
		return false;
	}
}//end clicked

function prepareEditor(){
	var editorBody=AJS.$('iframe').contents().find('body');
	editorBody.click(function(){clickedFunction()});	
}//prepare

function showError(n, err) {
    AJS.$('#mscgen-image').hide();
    AJS.$('#mscgen-preview-aui-msg,.aui-message.error').remove();
    AJS.messages.error("#mscgen-image-div", {
        id: "mscgen-preview-aui-msg",
        title: "Error.",
        body: "<p>" + err + "</p>",
        fadeout: true
    });
    markLine(n, err);
}

function markLine(n,err){
	editor.getSession().setAnnotations([{row:n-1, column:1,text:err,type:"error"}]);
}

function showPreviewImage(img){
	AJS.$('.aui-message.error').remove();
	editor.getSession().setAnnotations();
	var targetImage=AJS.$('#mscgen-image-div img');
	targetImage.attr('src','data:image/png;base64,'+img);
	targetImage.attr('alt',"mscgenImage");
	targetImage.css("margin-top","10%");
	targetImage.css("width","100%");
	targetImage.css("height","auto");
	AJS.$('#mscgen-image-div').css("overflow-y","auto");
	targetImage.attr('src',AJS.$('#mscgen-image-div img').clone().attr('src'));
	targetImage.show();
}

function mscgenPreview(rc){
	var code=editor.getValue();
	jQuery.ajax({
        url: AJS.contextPath()+"/plugins/servlet/mscgen_editor_preview?cuname="+AJS.params.currentUserFullname,
        type: 'POST',
        dataType: 'text',
        data: code,
        contentType: "application/json; charset=UTF-8",
        success: function (data) {
        	if(data.indexOf(",ERROR:")>=0){
        		var erd=data.split(",ERROR:");
        		showError(erd[0],erd[1]);		
        	}else{
        		showPreviewImage(data);
        	}
        	if(typeof(rc) == typeof(Function)){
        		rc();
        	}
        },
        error:function(data,status,er) {
           // console.log("error: "+data+" status: "+status+" er:"+er);
        }
    });
	
}
