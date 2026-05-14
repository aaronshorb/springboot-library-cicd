console.log('We are inside client.js');

/* on page load  */
window.onload = function() {
    fetch("/os", {
            method: "GET"
        })
        .then(function(res) {
            if (res.ok) {
                return res.json();
            }
            throw new Error('Request failed');
        }).catch(function(error) {
            console.log(error);
        })
        .then(function(data) {
            document.getElementById('hostname').innerHTML = `Running in Container: ${data.os}`
        });
};

const btn = document.getElementById('submit');
if (btn) {
    btn.addEventListener('click', func);
}

function func() {
    const book_id = document.getElementById("bookID").value
    console.log("onClick Submit - Request Book ID - " + book_id)

    fetch("/book", {
            method: "POST",
            body: JSON.stringify({
                id: document.getElementById("bookID").value
            }),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        })
        .then(function(res2) {
            if (res2.ok) {
                return res2.json();
            }
            throw new Error('Request failed.');
        }).catch(function(error) {
            alert("Please enter a valid book ID (0-6)")
            console.log(error);
        })
        .then(function(data) {
            if (data) {
                document.getElementById('resultCard').classList.add('active');
                document.getElementById('bookName').innerHTML = data.name;
                document.getElementById('bookAuthor').innerHTML = data.author;
                document.getElementById('bookEdition').innerHTML = data.edition;
                document.getElementById('bookDescription').innerHTML = data.description;

                const element = document.getElementById("bookImage");
                element.style.backgroundImage = "url(" + data.image + ")";
            }
        });
}
