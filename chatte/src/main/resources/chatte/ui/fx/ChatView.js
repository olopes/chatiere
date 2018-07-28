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
function getInputContents() {
	var result = {}, resources = [], images, i;
	images = document.body.getElementsByTagName("IMG");
	for (i = 0; i < images.length; i++) {
		resources.push(images[i].src.substring(6));
	}	
	result.resources = resources;
	result.text = document.body.innerHTML;
	return result;
}

function displayMessage(msg) {
	var msglist, msgEl;
	if(!msg) return;
	msglist = document.getElementById('msglist');
	msgEl = document.createElement('DIV');
	msgEl.innerHTML = msg;
	msglist.appendChild(msgEl);
	window.scrollTo(0,document.body.scrollHeight);
}

function clearScreen() {
	// faster than msglist.innerHTML=''
	// https://jsperf.com/innerhtml-vs-removechild/15
	var msglist = document.getElementById('msglist');
	while (msglist.firstChild) {
		msglist.removeChild(msglist.firstChild);
	}
}

function appendImage(resource) {
	var imgEl = createImageEl(resource);
	appendChildAtCursor(imgEl);
	return imgEl;
}

function appendEmoji(emoji) {
	var emoEl = document.createTextNode(emoji);
	appendChildAtCursor(emoEl);
	return emoEl;
}

function appendText(text) {
	var node = document.createTextNode(text);
	appendChildAtCursor(node);
	return node;
}

function appendChildAtCursor(node) {
    var range, node, sel;
    if(!node) return;
    document.body.focus();
    sel = window.getSelection();
    if (sel.rangeCount) {
        range = sel.getRangeAt(0);
        range.insertNode(node);
        
        range = document.createRange();
        range.selectNode(node);
       	range.setStartAfter(node);
       	range.setEndAfter(node);
        sel.removeAllRanges();
        sel.addRange(range);
    } else {
    	document.body.appendChild(node);
    }
}

function loadResources(resources) {
	var img, i, onclick;
	if(!resources) return;
	for(i = 0; i < resources.length; i++) {
		img = createImageEl(resources[i]);
		img.className='img-list';
		img.width = 100;
		document.body.appendChild(img);
	}
}

function createImageEl(resource) {
	var imgEl = document.createElement('IMG');
	imgEl.src="chato:"+resource;
	return imgEl;
}

function handleEmojiClick (evt) {
	if(evt.target.tagName === 'SPAN')
		doSelectEmoji(evt.target);
	return false;
}

function handleResourceClick (evt) {
	if(evt.target.tagName === 'IMG')
		doSelectResource(evt.target);
	return false;
}

// The application controller will capture the alert event and handle it accordingly
function doSelectResource(element) {
	alert(element.src);
}

function doSelectEmoji(element) {
	alert(element.innerText);
}

function killDragDropEvent(e) {
	e = e || event;
	e.preventDefault();
	return false;
}
window.addEventListener("dragover",killDragDropEvent,false);
window.addEventListener("drop",killDragDropEvent,false);

