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
package uk.co.flax.ukmp.twitter;

import java.util.List;
import java.util.Map;

/**
 * Interface for classes which need to be notified when the twitter update lists
 * change.
 *
 * @author Matt Pearce
 */
public interface TwitterListListener {

	/**
	 * Notify the listener class that the list IDs have changed.
	 * @param listIds the new map of list IDs.
	 */
	public void notify(Map<String, List<Long>> listIds);

}
