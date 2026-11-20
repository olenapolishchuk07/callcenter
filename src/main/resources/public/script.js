const tableBody = document.querySelector("#recordings");
const emptyState = document.querySelector("#empty-state");
const form = document.querySelector("#recording-form");
const recordingIdInput = document.querySelector("#recordingId");
const durationInput = document.querySelector("#duration");
const createdAtInput = document.querySelector("#createdAt");
const submitButton = document.querySelector("#submit-button");
const cancelButton = document.querySelector("#cancel-button");
const message = document.querySelector("#message");
let editingId = null;

function showMessage(text, success) {
    message.textContent = text;
    message.hidden = false;
    message.className = `message ${success ? "success" : "error"}`;
}

async function loadRecordings() {
    try {
        const response = await fetch("/api/CallCenter");
        const recordings = await response.json();
        if (!response.ok) {
            showMessage(recordings.message || "Не вдалося завантажити записи.", false);
            return;
        }
        tableBody.replaceChildren();
        emptyState.hidden = recordings.length !== 0;

        recordings.forEach((recording) => {
            const row = document.createElement("tr");
            [
                recording.recordingId,
                String(recording.duration),
                recording.createdAt
            ].forEach((value) => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            const actions = document.createElement("td");
            const edit = document.createElement("button");
            edit.type = "button";
            edit.className = "button secondary";
            edit.textContent = "Змінити";
            edit.addEventListener("click", () => startEdit(recording));

            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "button danger";
            remove.textContent = "Видалити";
            remove.addEventListener("click", () => deleteRecording(recording.recordingId));

            actions.append(edit, remove);
            row.appendChild(actions);
            tableBody.appendChild(row);
        });
    } catch (error) {
        showMessage("Не вдалося підключитися до сервера.", false);
    }
}

function startEdit(recording) {
    editingId = recording.recordingId;
    recordingIdInput.value = recording.recordingId;
    durationInput.value = String(recording.duration);
    createdAtInput.value = recording.createdAt;
    recordingIdInput.readOnly = true;
    submitButton.textContent = "Зберегти виправлення";
    cancelButton.hidden = false;
    recordingIdInput.focus();
}

function resetForm() {
    editingId = null;
    form.reset();
    recordingIdInput.readOnly = false;
    submitButton.textContent = "Зберегти запис";
    cancelButton.hidden = true;
}

async function deleteRecording(recordingId) {
    if (!window.confirm(`Видалити запис ${recordingId}?`)) {
        return;
    }
    await sendChange("DELETE", `/api/CallCenter/${encodeURIComponent(recordingId)}`);
}

async function sendChange(method, url, body) {
    try {
        const response = await fetch(url, {
            method,
            headers: body ? { "Content-Type": "application/json" } : undefined,
            body: body ? JSON.stringify(body) : undefined
        });
        const result = await response.json();
        if (!response.ok) {
            showMessage(result.message || "Дію не виконано.", false);
            return;
        }
        showMessage(result.message || "Дію успішно виконано.", true);
        resetForm();
        await loadRecordings();
    } catch (error) {
        showMessage("Не вдалося виконати запит до сервера.", false);
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const durationText = durationInput.value.trim();
    const parsedDuration = durationText === "" ? null : Number(durationText);
    const body = {
        recordingId: recordingIdInput.value,
        duration: Number.isInteger(parsedDuration) ? parsedDuration : null,
        createdAt: createdAtInput.value
    };
    if (editingId) {
        await sendChange("PUT", `/api/CallCenter/${encodeURIComponent(editingId)}`, body);
    } else {
        await sendChange("POST", "/api/CallCenter", body);
    }
});

cancelButton.addEventListener("click", resetForm);
loadRecordings();
