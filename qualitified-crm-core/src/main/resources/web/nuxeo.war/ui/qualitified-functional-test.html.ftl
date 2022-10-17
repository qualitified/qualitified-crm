<dom-module id="qualitified-functional-test">
    <template>
        <style>
			#testDialog{
				z-index: 9999999999;
			}
		</style>
        <nuxeo-dialog id="testDialog" on-iron-overlay-closed="_dialogClosed" with-backdrop>
            <div id="content" class="content">
	    	<span id="success" style="display:none">
	    		<paper-icon-button id="button" icon="icons:check-circle" style="color:#2bd670" role="button" tabindex="0" aria-disabled="false"></paper-icon-button>
	    		Success!
	    	</span>
                <span id="failure" style="display:none">
	    		<paper-icon-button id="ko" class="status" icon="icons:cancel" style="color:#f50808" role="button" tabindex="0" aria-disabled="false"></paper-icon-button>
	    		Failure!
	    		<br/>
	    		<span id="message"></span>
	    	</span>
            </div>
            <div class="buttons">
                <paper-button class="primary" noink dialog-dismiss on-tap="_close">Close</paper-button>
            </div>
        </nuxeo-dialog>
    </template>
    <script>
	    Polymer({
	      is: 'qualitified-functional-test',
	      properties: {
	      	result: {
	      		type: String,
	      		value: ""
	      	}
	      },
	      ready: function(){
	    	let urlParams = this._getQueryParams();
	    	if(urlParams.length == 0 || !urlParams["test"]){
	    		return;
	    	}
	    	var nuxeoApp = document.querySelector("nuxeo-app");
	    	var nuxeoHome = nuxeoApp.shadowRoot.querySelector("nuxeo-home");
	      	console.log("Initializing test...");
	      	console.log("Wait 3 sec...");
	      	setTimeout(function () {
				runScenario();
	      	}, 3000);

	      	${qualitified.functional.test.scenario}
	      	eval(this.document.properties["note:note"]);

	      	function assertEquals(expected, result, message){
            	let status = expected === result ? "success": "failure";
            	nuxeoHome.shadowRoot.querySelector("qualitified-unit-test").shadowRoot.querySelector("#"+status).style.display = "block";
            	nuxeoHome.shadowRoot.querySelector("qualitified-unit-test").shadowRoot.querySelector("#message").innerHTML = message;
            	nuxeoApp.shadowRoot.appendChild(nuxeoHome.shadowRoot.querySelector("qualitified-unit-test").shadowRoot.querySelector("#testDialog"));
            	nuxeoApp.shadowRoot.querySelector("#testDialog").toggle();
            	console.log(status);
            }

	      },

	      _getQueryParams: function () {
                let params = {};
                this.path = window.decodeURIComponent(window.location.pathname);
                if (window.location.hash.includes('?')) {
                    const parts = window.location.hash.split('?');
                    const query = parts[1];
                    const queryParams = query.split('&');
                    queryParams.forEach((queryParam) => {
                        const p = queryParam.split('=');
                        if (p.length > 0) {
                            params[p[0]] = p[1] ? p[1] : '';
                        }
                    });
                }
                return params;
            },
            _close: function () {
            	Window.close();
            }

	    });
	</script>
</dom-module>