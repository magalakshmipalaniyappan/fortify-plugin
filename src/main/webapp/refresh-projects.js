/*******************************************************************************
 * (c) Copyright 2019 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
// to refresh the SSC applications list
function refreshProjectNames(url, elm)
{
    var typedText = elm.parentNode.previousSibling.querySelector("input.project-name").value;

    //var spinner = document.getElementById('refreshSpinner');
    var spinner = elm.parentNode.nextSibling;
    spinner.style.display="block";
    //var buttonName = document.getElementById('refreshPrjButton');
    var buttonName = elm;
    buttonName.disabled=true;
    //var buttonVer = document.getElementById('refreshPrjVerButton');
    //buttonVer.disabled=true;
    var buttonVer = elm.parentNode.parentNode.parentNode.nextSibling;
    if (buttonVer.classList.contains("validation-error-area")) {
        buttonVer = buttonVer.nextSibling.nextSibling;
    }
    buttonVer = buttonVer.querySelector("input.project-version");
    buttonVer.disabled=true;

    var parameters = { typedText };

    new Ajax.Request(url, {
        parameters: parameters,
        onComplete: function(rsp) {
            spinner.style.display="none";
            var jsr = tryParseJSON(rsp.responseText);
            if (!jsr) {
                return;
            }
            //var select = document.getElementById('projectName');
            var select = elm.parentNode.previousSibling.querySelector("input.project-name");
            var oldSelect = select.value;
            if (select) {
                var items = new Array();
                var selectedIndex = 0;
                // add new values
                for(var i=0; i<jsr.list.length; i++) {
                    var item = jsr.list[i];
                    items.push(DOMPurify.sanitize(item.name));
                    if (oldSelect==item.name) {
                        selectedIndex = items.length-1;
                    }
                }
                updateComboBox(select.comboBox, items, selectedIndex);
            }

            buttonVer.value = ''; // clear version selection

            buttonName.disabled=false;
            buttonVer.disabled=false;
        }
    });
}

function tryParseJSON(jsonString){
    try {
        // this is better than using eval(), provides some escaping, see Bug 55768
        var o = JSON.parse(jsonString);
        if (o && typeof o === "object") {
            return o;
        }
    } catch (e) {
        // ignore
    }
    return false;
};

//to refresh the SSC application version list
function refreshProjectVersions(url, elm)
{
    //var spinner = document.getElementById('refreshSpinner');
    var spinner = elm.parentNode.nextSibling;
    spinner.style.display="block";

    var selectedPrj = elm.parentNode.parentNode.parentNode.previousSibling;
    if (selectedPrj.classList.contains("help-area")) {
        selectedPrj = selectedPrj.previousSibling.previousSibling;
    }
    selectedPrj = selectedPrj.querySelector("input.project-name").value;

    var typedText = elm.parentNode.previousSibling.querySelector("input.project-version").value;

    var parameters = { selectedPrj, typedText };

    //var buttonVer = document.getElementById('refreshPrjVerButton');
    var buttonVer = elm;
    buttonVer.disabled=true;

    new Ajax.Request(url, {
        parameters: parameters,
        onComplete: function(rsp) {
            spinner.style.display="none";
            buttonVer.disabled=false;

            var jsr = tryParseJSON(rsp.responseText);
            if (!jsr) {
                return;
            }
            //var select = document.getElementById('projectVersion');
            var select = elm.parentNode.previousSibling.querySelector("input.project-version");
            //var selectedPrj = document.getElementById('projectName').value;
            var selectedPrj = elm.parentNode.parentNode.parentNode.previousSibling;
            if (selectedPrj.classList.contains("help-area")) {
                selectedPrj = selectedPrj.previousSibling.previousSibling;
            }
            selectedPrj = selectedPrj.querySelector("input.project-name").value;
            var oldSelect = select.value;
            if (select) {
                var items = new Array();
                var selectedIndex = 0;
                // add new values
                for(var i=0; i<jsr.list.length; i++) {
                    var item = jsr.list[i];
                    if (item.prj == selectedPrj || selectedPrj === "") {
                        items.push(DOMPurify.sanitize(item.name));
                        if (oldSelect==item.name) {
                            selectedIndex = items.length-1;
                        }
                    }
                }
                updateComboBox(select.comboBox, items, selectedIndex);
            }
        }
    });
}

function refreshTemplateList(url,paramList)
{
    var parameters = {};
    paramList.split(',').each(function(name) {
        var p = document.getElementById(name);
        if (p==null) {
            p = document.getElementsByName(name);
            if (p != null && p.length > 0) {
                p = p[0];
            } else {
                p = document.getElementsByName("_."+name);
                if (p != null && p.length > 0) {
                    p = p[0];
                }
            }
        }
        if(p!=null) {
            if(p.type=="checkbox")  parameters[name] = p.checked;
            else                    parameters[name] = p.value;
        }
    });

    var spinner = document.getElementById('refreshSpinner');
    spinner.style.display="block";
    var button = document.getElementById('refreshButton');
    button.disabled=true;

    new Ajax.Request(url, {
        parameters: parameters,
        onComplete: function(rsp) {
            spinner.style.display="none";
            var jsr = tryParseJSON(rsp.responseText);
            if (!jsr) {
                return;
            }
            var select = document.getElementById('projectTemplate');
            var oldSelect = select.value;
            if ( select ) {
                var items = new Array();
                var selectedIndex = 0;
                // add new values
                for(var i=0; i<jsr.list.length; i++) {
                    var item = jsr.list[i];
                    items.push(item.name);
                    if (oldSelect==item.name) {
                        selectedIndex = items.length-1;
                    }
                }
                updateComboBox(select.comboBox, items, selectedIndex);
            }
        }
    });
    button.disabled=false;
}

function updateComboBox(comboBox, items, selectedIndex) {
    comboBox.field.focus();
    comboBox.field.moveCaretToEnd();
    /*
		Workaround for IE. IE has different from other browsers event queue.
		It leads to incorrect behavior because field.onFocus function is called after setItems and clear the list
		Zero timeout allows to set items after all event handlers like onFocus are executed
	*/
    setTimeout (function() {
        comboBox.setItems(items);
        comboBox.select(selectedIndex);
        /*
                tdList = comboBox.field.parentElement.parentElement.parentElement.nextElementSibling.getElementsByTagName("td");
                for (var td = 0; td < tdList.length; td++) {
                    divList = tdList[td].getElementsByTagName("div");
                    for (var i = 0; i < divList.length; i++) {
                        div = divList[i];
                        if (div.className == "error") {
                            while (div.childNodes.length > 0) {
                                div.removeChild(div.childNodes[0]);
                            }
                        }
                    }
                }
        */
    }, 0);
}

function refreshSensorPools(url, elm) {
    var parameters = {};

    //var poolsButton = document.getElementById('refreshSensorPoolsButton');
    var poolsButton = elm;
    poolsButton.disabled=true;

    //var spinner = document.getElementById('refreshSpinner');
    var spinner = elm.parentNode.nextSibling;
    spinner.style.display="block";

    new Ajax.Request(url, {
        parameters: parameters,
        onComplete: function(rsp) {
            spinner.style.display="none";

            var jsr = tryParseJSON(rsp.responseText);
            if (!jsr) {
                return;
            }
            //var select = document.getElementById('sensorPoolName');
            var select = elm.parentNode.previousSibling.querySelector("input.sensor-pool-name");
            var oldSelect = select.value;
            if (select) {
                var items = new Array();
                var selectedIndex = 0;
                // add new values
                for(var i=0; i<jsr.list.length; i++) {
                    var item = jsr.list[i];
                    items.push(item.name);
                    if (oldSelect==item.name) {
                        selectedIndex = items.length-1;
                    }
                }
                updateComboBox(select.comboBox, items, selectedIndex);
            }

            poolsButton.disabled=false;
        }
    });
}

var readOnlyElms = document.getElementsByClassName("read-only");
for (var i = 0; i < readOnlyElms.length; i++) {
    readOnlyElms[i].setAttribute("readonly", "true");
}


var sscUrl = document.getElementById("url");
if (sscUrl != null) {
    sscUrl.addEventListener("input", disableCtrlURLInput);
}

function disableCtrlURLInput() {
    var ctrlUrl = document.getElementById("ctrlUrl");
    if (ctrlUrl != null) {
        if (sscUrl.value.length > 0) {
            ctrlUrl.setAttribute("disabled", "true");
        } else {
            ctrlUrl.removeAttribute("disabled");
        }
    }
}

window.addEventListener("load", disableCtrlURLInput);
