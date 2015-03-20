/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.flax.ukmp.config;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Created by mlp on 14/03/15.
 */
public class TwitterConfiguration {

    private String authConfigFile;

    private int deletionBatchSize = 50;

    private int statusBatchSize = 100;

    private boolean enabled;

    private List<TwitterListConfiguration> lists;

    private int updateCheckHours;

    @NotNull
    private String dataDirectory;

    public boolean isEnabled() {
        return enabled;
    }

    public String getAuthConfigFile() {
        return authConfigFile;
    }

    public int getDeletionBatchSize() {
        return deletionBatchSize;
    }

    public int getStatusBatchSize() {
        return statusBatchSize;
    }

    public List<TwitterListConfiguration> getLists() {
        return lists;
    }

    public int getUpdateCheckHours() {
        return updateCheckHours;
    }

	/**
	 * @return the dataDirectory
	 */
	public String getDataDirectory() {
		return dataDirectory;
	}

}
