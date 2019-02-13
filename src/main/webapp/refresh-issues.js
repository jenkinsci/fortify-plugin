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
		var isUpdateEnable = true;
		
		function updateByUrl(boxId,urlLink,spinnerUrl) {
            // first display the "loading..." icon
        	if (isUpdateEnable) {
        		isUpdateEnable = false;
        		var box = document.getElementById(boxId);
        		box.innerHTML = '<img src="' + spinnerUrl + '" alt=""/>';
        		// then actually fetch the HTML
        		new Ajax.Request(urlLink, {
        			method: "post",
        			onComplete: function(rsp,_) {
        				var issueTable = document.getElementById('issueTable');
        				if (issueTable != null) {
        					issueTable.innerHTML = rsp.responseText;
        				}
        				isUpdateEnable = true;
        			}
        		});
        	}
        }

        function updateList(boxId,folder,nextPage,spinnerUrl) {
            updateByUrl(boxId,contextUrl+"/updateIssueList?folder=" + folder + "&page=" + nextPage,spinnerUrl);
        }

        function updateListWithSort(boxId,folder,nextPage,sortOrd,spinnerUrl) {
            updateByUrl(boxId,contextUrl+"/updateIssueList?folder=" + folder + "&page=" + nextPage + "&sort=" + sortOrd,spinnerUrl);
        }
        
        function updatePageSize(boxId,aSize,spinnerUrl) {
        	updateByUrl(boxId,contextUrl+"/setPageSize?size=" + aSize,spinnerUrl);
        }

        function showNew(boxId,spinnerUrl) {
            updateByUrl(boxId,contextUrl+"/showAllNotNew?all=no",spinnerUrl);
        }

        function showAll(boxId,spinnerUrl) {
            updateByUrl(boxId,contextUrl+"/showAllNotNew?all=yes",spinnerUrl);
        }

        function showGrouping(boxId,selectedGrouping,spinnerUrl)
        {
            updateByUrl(boxId,contextUrl+"/selectedGrouping?grouping="+selectedGrouping, spinnerUrl);
        }

        function scheduleUpdateCheck() {
            var parameters = {};
            new Ajax.Request(contextUrl+"/checkUpdates?stamp="+stamp,{
                method: "get",
                onComplete: function(rsp,_) {
                    var update = rsp.getResponseHeader('go');
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
            var parameters = {};
            new Ajax.Request(contextUrl+"/ajaxStats",{
                method: "get",
                onComplete: function(rsp,_) {
                    var scanStatistics = document.getElementById('scanStatistics');
                    if (scanStatistics != null) {
                        scanStatistics.innerHTML = rsp.responseText;
                    }
                }
            });
        }

        function reloadIssues() {
            var parameters = {};
            new Ajax.Request(contextUrl+"/ajaxIssues",{
                parameters: parameters,
                onComplete: function(rsp) {
                    var issueTable = document.getElementById('issueTable');
                    if (issueTable != null) {
                        issueTable.innerHTML = rsp.responseText;
                    }
                }
            });
        }

        function reload(url,box) {
            var parameters = {};
            new Ajax.Request(url,{
                parameters: parameters,
                onComplete: function(rsp) {
                    var issueTable = document.getElementById(box);
                    if (issueTable != null) {
                        issueTable.innerHTML = rsp.responseText;
                    }
                }
            });
        }

        function loadIssueTable(spinnerUrl) {
           function loadIssues() {
               // first display the "loading..." icon
               var box = document.getElementById('firstTimeSpinF');
               box.innerHTML = '<img src="'+spinnerUrl+'" alt=""/>';
               // then actually fetch the HTML
               var request = new Ajax.Request(contextUrl+"/ajaxIssues?firstTime=yes",{
                   method: "get",
                   onComplete: function(rsp,_) {
                       var issueTable = document.getElementById('issueTable');
                       issueTable.innerHTML = rsp.responseText;
                       // next update
                       // window.setTimeout(loadIssues, 5000);
                       window.setTimeout(scheduleUpdateCheck, 10000);
                   }
               });
            }
            window.setTimeout(loadIssues, 0);
        }
