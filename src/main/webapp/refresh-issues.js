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
        var isUpdateEnable = true;
        function updateByUrl(boxId,urlLink,params,spinnerUrl) {
            // first display the "loading..." icon
            if (isUpdateEnable) {
                isUpdateEnable = false;
                var box = document.getElementById(boxId);
                box.innerHTML = '<img src="' + spinnerUrl + '" alt=""/>';
                const parameters = [];
                for (const key in params) {
                    if (params.hasOwnProperty(key)) {
                        parameters.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`);
                    }
                }
                 // then actually fetch the HTML
                fetch(urlLink + "?" + parameters.join('&'), {
                    method: 'POST',
                    headers: crumb.wrap({
                        'Content-Type': 'text/plain'
                    })
                })
                .then(response => response.text())
                .then(text => {
                    var issueTable = document.getElementById('issueTable');
                    if (issueTable != null) {
                        issueTable.innerHTML = text;
                    }
                    isUpdateEnable = true;
                })
                .catch((error) => {
                    // Handle any errors
                });
            }
        }

        function updateList(boxId,folder,nextPage,spinnerUrl) {
            var params = {'folder' : folder, 'page' : nextPage};
            updateByUrl(boxId,contextUrl+"/updateIssueList",params,spinnerUrl);
        }

        function updateListWithSort(boxId,folder,nextPage,sortOrd,spinnerUrl) {
            var params = {'folder' : folder, 'page' : nextPage, 'sort' : sortOrd};
            updateByUrl(boxId,contextUrl+"/updateIssueList",params,spinnerUrl);
        }
        
        function updatePageSize(boxId,aSize,spinnerUrl) {
            var params = {'size' : aSize};
            updateByUrl(boxId,contextUrl+"/setPageSize",params,spinnerUrl);
        }

        function showNew(boxId,spinnerUrl) {
            var params = 'all=no';
            updateByUrl(boxId,contextUrl+"/showAllNotNew",params,spinnerUrl);
        }

        function showAll(boxId,spinnerUrl) {
            var params = 'all=yes';
            updateByUrl(boxId,contextUrl+"/showAllNotNew",params,spinnerUrl);
        }

        function showGrouping(boxId,selectedGrouping,spinnerUrl) {
            var params = {'grouping' :  selectedGrouping};
            updateByUrl(boxId,contextUrl+"/selectedGrouping",params, spinnerUrl);
        }

        function scheduleUpdateCheck() {
            fetch(contextUrl + "/checkUpdates?stamp=" + stamp, {
                method: 'POST',
                headers: crumb.wrap({
                    'Content-Type': 'text/plain'
                })
            }).then(function(rsp) {
                if (rsp.ok) {
                    var update = rsp.headers.get('go');
                    if(update == "go") {
                        stamp = new Date().getTime();
                        reloadStatistics();
                        reloadIssues();
                    }
                    // next update in 10 sec
                    window.setTimeout(scheduleUpdateCheck, 10000);
                }
            });
        }

        function reloadStatistics() {
            fetch(contextUrl + "/ajaxStats", {
                method: 'POST',
                headers: crumb.wrap({
                    'Content-Type': 'text/plain'
                })
            })
            .then(response => response.text())
            .then(text => {
                var scanStatistics = document.getElementById('scanStatistics');
                if (scanStatistics != null) {
                    scanStatistics.innerHTML = text;
                }
            })
            .catch((error) => {
                // Handle any errors
            });
        }

        function reloadIssues() {
            fetch(contextUrl + "/ajaxIssues", {
                method: 'POST',
                headers: crumb.wrap({
                    'Content-Type': 'text/plain'
                })
            })
            .then(response => response.text())
            .then(text => {
                var issueTable = document.getElementById('issueTable');
                if (issueTable != null) {
                    issueTable.innerHTML = text;
                }
            })
            .catch((error) => {
                // Handle any errors
            });
        }

        function reload(url,box) {
            fetch(url, {
                method: 'POST',
                headers: crumb.wrap({
                    'Content-Type': 'text/plain'
                })
            })
            .then(response => response.text())
            .then(text => {
                var issueTable = document.getElementById(box);
                if (issueTable != null) {
                    issueTable.innerHTML = text;
                }
            })
            .catch((error) => {
                // Handle any errors
            });
        }

        function loadIssueTable(spinnerUrl) {
           function loadIssues() {
               // first display the "loading..." icon
               var box = document.getElementById('firstTimeSpinF');
               box.innerHTML = '<img src="'+spinnerUrl+'" alt=""/>';
               // then actually fetch the HTML
                fetch(contextUrl + "/ajaxIssues?firstTime=yes", {
                    method: 'POST',
                    headers: crumb.wrap({
                        'Content-Type': 'text/plain'
                    })
                })
                .then(response => response.text())
                .then(text => {
                   var issueTable = document.getElementById('issueTable');
                   issueTable.innerHTML = text;
                   // next update
                   window.setTimeout(scheduleUpdateCheck, 10000);
                })
                .catch((error) => {
                    // Handle any errors
                });
            }
            window.setTimeout(loadIssues, 0);
        }
