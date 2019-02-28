const chatApplication = (() => {

    const chatTextAreaId = "chat";

    function postJson(data) {
        const xhr = new XMLHttpRequest();
        const url = "api/messages";
        xhr.open("POST", url, true);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.onreadystatechange = function () {
            if (xhr.readyState !== 4 && xhr.status > 300) {
                alert("Failed to post message to the server: " + xhr.status);
            } else {
                // Scroll down automatically when having published a new message
                const chat = document.getElementById(chatTextAreaId);
                chat.scrollTop = chat.scrollHeight;
            }
        };
        const jsonData = JSON.stringify(data);
        xhr.send(jsonData);
    }

    function postMessage() {
        function isEmpty(str) {
            return !str || str.trim() === ""
        }

        const from = document.getElementById("from").value;
        if (isEmpty(from)) {
            alert("You need to enter your name");
            return false;
        }
        const message = document.getElementById("message").value;
        if (isEmpty(message)) {
            alert("You need to enter a message");
            return false;
        }
        const data = {from: from, text: message};
        postJson(data)
    }

    function subscribeToMessages() {
        const source = new EventSource("api/messages");

        source.addEventListener('message', e => {
            const messageJson = JSON.parse(e.data);
            const messageToShow = `[${messageJson.from}] ${messageJson.text}\n`;
            document.getElementById(chatTextAreaId).innerHTML += messageToShow;
        }, false);

        source.addEventListener('sentiment', e => {
            document.getElementById("sentiment").innerHTML = `Chat happiness is: ${e.data}`;
        }, false);
    }

    function start() {
        document.getElementById("message")
                .addEventListener('keyup', e => {
                    if (e.key === "Enter") {
                        postMessage();
                        document.getElementById("message").value = ""
                    }
                });
        subscribeToMessages();
    }

    return {
        start: start
    };
})();

window.onload = chatApplication.start;
