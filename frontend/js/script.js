document.getElementById('claimForm').addEventListener('submit', function(e) {
        e.preventDefault(); // Evita que la página recargue
        
        const terminal = document.getElementById('terminal-output');
        terminal.style.display = 'block';
        terminal.innerText = "Conectando con el middleware Java...";

        const payload = {
            policyId: document.getElementById('policyId').value,
            customerId: document.getElementById('customerId').value,
            claimAmount: parseFloat(document.getElementById('claimAmount').value),
            userId: document.getElementById('userId').value,
            userRole: document.getElementById('userRole').value
        };

        fetch('http://localhost:8080/api/claims', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(response => response.json())
        .then(data => {
            if(data.status === "PROCESSED" && data.mainframe_log) {
                terminal.innerText = data.mainframe_log.join('\n');
            } else {
                terminal.innerText = "Error: " + data.message;
            }
        })
        .catch(error => {
            terminal.innerText = "Error de conexión: " + error.message;
        });
    });