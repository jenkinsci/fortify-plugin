/*******************************************************************************
 * Copyright 2023 Open Text.
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

    fetch(url, {
        method: 'POST',
        headers: crumb.wrap({
            'Content-Type': 'application/x-www-form-urlencoded'
        }),
        body: new URLSearchParams({
            typedText: typedText,
        })
    })
    .then((response) => response.json())
    .then((data) => {
        spinner.style.display="none";
        var select = elm.parentNode.previousSibling.querySelector("input.project-name");
        var oldSelect = select.value;
        if (select) {
            var items = new Array();
            var selectedIndex = 0;
            // add new values
            for (var i=0; i<data.list.length; i++) {
                var item = data.list[i];
                items.push(DOMPurify.sanitize(item.name));
                if (oldSelect == item.name) {
                    selectedIndex = items.length-1;
                }
            }
            updateComboBox(select.comboBox, items, selectedIndex);
        }
        buttonVer.value = ''; // clear version selection
        buttonName.disabled=false;
        buttonVer.disabled=false;
    })
    .catch((error) => {
        console.error(error);
    });
}

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

    //var buttonVer = document.getElementById('refreshPrjVerButton');
    var buttonVer = elm;
    buttonVer.disabled=true;

    fetch(url, {
        method: 'POST',
        headers: crumb.wrap({
            'Content-Type': 'application/x-www-form-urlencoded'
        }),
        body: new URLSearchParams({
            selectedPrj: selectedPrj,
            typedText: typedText,
        })
    })
    .then((response) => response.json())
    .then((data) => {
            spinner.style.display="none";
            buttonVer.disabled=false;

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
                for(var i=0; i<data.list.length; i++) {
                    var item = data.list[i];
                    if (item.prj == selectedPrj || selectedPrj === "") {
                        items.push(DOMPurify.sanitize(item.name));
                        if (oldSelect==item.name) {
                            selectedIndex = items.length-1;
                        }
                    }
                }
                updateComboBox(select.comboBox, items, selectedIndex);
            }
    })
    .catch((error) => {
        console.error(error);
    });
}

function refreshTemplateList(url,paramList)
{
    var parameters = new URLSearchParams();
    paramList.split(',').forEach(function(name) {
        var p = document.getElementById(name);
        if (p == null) {
            p = document.getElementsByName(name);
            if (p != null && p.length > 0) {
                p = p[0];
            } else {
                p = document.getElementsByName("_." + name);
                if (p != null && p.length > 0) {
                    p = p[0];
                }
            }
        }
        if (p != null) {
            if (p.type == "checkbox") {
                parameters.append(name, p.checked);
            } else {
                parameters.append(name, p.value);
            }
        }
    });

    var spinner = document.getElementById('refreshSpinner');
    spinner.style.display="block";
    var button = document.getElementById('refreshButton');
    button.disabled=true;

    fetch(url, {
        method: 'POST',
        headers: crumb.wrap({
            'Content-Type': 'application/x-www-form-urlencoded'
        }),
        body: parameters
    })
    .then((response) => response.json())
    .then((data) => {
            spinner.style.display="none";
            var select = document.getElementById('projectTemplate');
            var oldSelect = select.value;
            if ( select ) {
                var items = new Array();
                var selectedIndex = 0;
                // add new values
                for(var i=0; i<data.list.length; i++) {
                    var item = data.list[i];
                    items.push(item.name);
                    if (oldSelect==item.name) {
                        selectedIndex = items.length-1;
                    }
                }
                updateComboBox(select.comboBox, items, selectedIndex);
            }
    })
    .catch((error) => {
        console.error(error);
    });
    button.disabled=false;
}

function updateComboBox(comboBox, items, selectedIndex) {
    comboBox.field.focus();
    comboBox.field.moveCaretToEnd();
    comboBox.setItems(items);
    comboBox.select(selectedIndex);
    comboBox.field.parentNode.addEventListener('focusout', function (e) {
        fireEvent(comboBox.field, "change"); // to trigger dependent fields to update
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
