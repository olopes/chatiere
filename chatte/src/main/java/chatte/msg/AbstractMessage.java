/*
 * MIT License
 * 
 * Copyright (c) 2018 OLopes
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package chatte.msg;

import java.io.Serializable;
import java.util.Set;

public abstract class AbstractMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	transient boolean complete;
	transient Friend from;
	transient boolean resourcesRequested;
	transient boolean remote;

	public AbstractMessage() {
	}

	public AbstractMessage(Friend from) {
		this.from = from;
	}

	public Friend getFrom() {
		return from;
	}

	public void setFrom(Friend from) {
		this.from = from;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public Set<String> getResourceRefs() {
		return null;
	}
	public void setResourceRefs(Set<String> resourceRefs) {
	}


	public boolean isResourcesRequested() {
		return resourcesRequested;
	}

	public void setResourcesRequested(boolean resourcesRequested) {
		this.resourcesRequested = resourcesRequested;
	}

	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

}
