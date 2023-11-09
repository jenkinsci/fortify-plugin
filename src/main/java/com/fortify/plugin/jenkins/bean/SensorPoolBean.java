/*******************************************************************************
 * Copyright 2021-2023 Open Text.
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
package com.fortify.plugin.jenkins.bean;

public class SensorPoolBean implements Comparable {

    private String name;
    private String uuid;

    public SensorPoolBean() {}

    public SensorPoolBean(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SensorPoolBean) {
            return ((SensorPoolBean)obj).getUuid().equals(this.getUuid());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public int compareTo(Object o) {
        SensorPoolBean other = (SensorPoolBean)o;
        return this.getName().compareTo(other.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
