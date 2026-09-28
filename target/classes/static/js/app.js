// EventEase — Frontend Application Logic

let allEventsCache = [];
let allStudentsCache = [];
let allOrganizersCache = [];

document.addEventListener('DOMContentLoaded', () => {
    initNav();
    loadAllData();
});

// Navigation Handling
function initNav() {
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            switchTab(targetTab);
        });
    });
}

function switchTab(tabId) {
    document.querySelectorAll('.tab-pane').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));

    const activeTab = document.getElementById(tabId);
    if (activeTab) activeTab.classList.add('active');

    const activeBtn = document.querySelector(`[data-tab="${tabId}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    if (tabId === 'eventsTab') loadAllEvents();
    if (tabId === 'registerTab') refreshRegistrationDropdowns();
    if (tabId === 'createEventTab') refreshOrganizerDropdown();
    if (tabId === 'myRegistrationsTab') refreshStudentFilterDropdown();
}

// Initial Data Load
async function loadAllData() {
    await Promise.all([
        loadAllEvents(),
        loadStudents(),
        loadOrganizers()
    ]);
}

// ==========================================
// FEATURE 1: Events Loading & Rendering
// ==========================================
async function loadAllEvents() {
    const grid = document.getElementById('eventsGrid');
    grid.innerHTML = '<div class="loading-state">Loading campus events...</div>';

    try {
        const response = await fetch('/api/events');
        if (!response.ok) throw new Error('Failed to load events');
        const events = await response.json();
        allEventsCache = events;

        if (events.length === 0) {
            grid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    <p>No events scheduled yet. Click "Create New Event" to publish one!</p>
                </div>`;
            return;
        }

        grid.innerHTML = events.map(evt => {
            const isFull = evt.availableSeats <= 0;
            const percentageFilled = Math.min(100, Math.round((evt.activeRegistrationsCount / evt.maxSeats) * 100));

            return `
                <div class="glass-card event-card">
                    <div class="event-card-top">
                        <div class="event-badges">
                            <span class="event-badge ${isFull ? 'badge-full' : 'badge-available'}">
                                ${isFull ? '● EVENT FULL' : `● ${evt.availableSeats} SEAT${evt.availableSeats === 1 ? '' : 'S'} LEFT`}
                            </span>
                            <span class="event-id-tag">ID: #${evt.id}</span>
                        </div>
                        <h2 class="event-card-title">${escapeHtml(evt.title)}</h2>
                        <div class="event-meta-list">
                            <div class="event-meta-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                <span><strong>Date:</strong> ${evt.eventDate}</span>
                            </div>
                            <div class="event-meta-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                                <span><strong>Venue:</strong> ${escapeHtml(evt.venue)}</span>
                            </div>
                            <div class="event-meta-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                                <span><strong>Organizer:</strong> ${escapeHtml(evt.organizerName || 'College Faculty')}</span>
                            </div>
                        </div>

                        <!-- Capacity Bar -->
                        <div class="capacity-box">
                            <div class="capacity-labels">
                                <span>Registrations: ${evt.activeRegistrationsCount} / ${evt.maxSeats}</span>
                                <span>${percentageFilled}% Full</span>
                            </div>
                            <div class="capacity-bar-track">
                                <div class="capacity-bar-fill ${isFull ? 'bar-fill-full' : 'bar-fill-normal'}" style="width: ${percentageFilled}%"></div>
                            </div>
                        </div>
                    </div>

                    <div class="event-card-actions">
                        <button class="btn btn-primary btn-sm btn-block" 
                                onclick="quickRegisterForEvent(${evt.id})" 
                                ${isFull ? 'disabled' : ''}>
                            ${isFull ? 'Capacity Reached' : 'Register Now'}
                        </button>
                        <button class="btn btn-secondary btn-sm" onclick="openParticipantsModal(${evt.id}, '${escapeHtml(evt.title)}')">
                            Participants (${evt.activeRegistrationsCount})
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        grid.innerHTML = `<div class="empty-state"><p style="color: var(--rose);">Error loading events: ${err.message}</p></div>`;
    }
}

// ==========================================
// FEATURE 1: Create Event Submission
// ==========================================
async function handleCreateEvent(event) {
    event.preventDefault();
    const btn = document.getElementById('btnSubmitEvent');
    btn.disabled = true;
    btn.innerText = 'Publishing...';

    const payload = {
        title: document.getElementById('eventTitle').value.trim(),
        eventDate: document.getElementById('eventDate').value,
        venue: document.getElementById('eventVenue').value.trim(),
        maxSeats: parseInt(document.getElementById('eventMaxSeats').value, 10),
        organizerId: parseInt(document.getElementById('eventOrganizerSelect').value, 10)
    };

    try {
        const response = await fetch('/api/events', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Failed to create event');
        }

        showToast(`Event "${data.title}" created successfully!`, 'success');
        document.getElementById('createEventForm').reset();
        await loadAllEvents();
        switchTab('eventsTab');
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Publish Event';
    }
}

// ==========================================
// FEATURES 2 & 3: Student Registration
// ==========================================
function quickRegisterForEvent(eventId) {
    switchTab('registerTab');
    const select = document.getElementById('regEventSelect');
    if (select) {
        select.value = eventId;
        handleEventSelectionChange();
    }
}

function handleEventSelectionChange() {
    const eventId = document.getElementById('regEventSelect').value;
    const infoBox = document.getElementById('selectedEventInfo');

    if (!eventId) {
        infoBox.style.display = 'none';
        return;
    }

    const evt = allEventsCache.find(e => e.id == eventId);
    if (!evt) return;

    const isFull = evt.availableSeats <= 0;
    infoBox.style.display = 'block';
    infoBox.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <span><strong>Event:</strong> ${escapeHtml(evt.title)}</span>
            <span class="event-badge ${isFull ? 'badge-full' : 'badge-available'}">
                ${isFull ? 'FULL' : `${evt.availableSeats} SEAT(S) AVAILABLE`}
            </span>
        </div>
        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">
            Date: ${evt.eventDate} | Venue: ${escapeHtml(evt.venue)}
        </div>
    `;

    const submitBtn = document.getElementById('btnSubmitRegistration');
    if (isFull) {
        submitBtn.disabled = true;
        submitBtn.innerText = 'Event is Full (Capacity Reached)';
    } else {
        submitBtn.disabled = false;
        submitBtn.innerText = 'Complete Registration';
    }
}

async function handleRegistration(event) {
    event.preventDefault();
    const btn = document.getElementById('btnSubmitRegistration');
    btn.disabled = true;
    btn.innerText = 'Registering...';

    const payload = {
        studentId: parseInt(document.getElementById('regStudentSelect').value, 10),
        eventId: parseInt(document.getElementById('regEventSelect').value, 10)
    };

    try {
        const response = await fetch('/api/registrations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Registration failed');
        }

        showToast(`Registration Successful! ${data.studentName} is registered for ${data.eventTitle}.`, 'success');
        document.getElementById('registrationForm').reset();
        document.getElementById('selectedEventInfo').style.display = 'none';
        await loadAllEvents();
        switchTab('eventsTab');
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Complete Registration';
    }
}

// ==========================================
// FEATURE 4: View Registered Participants
// ==========================================
async function openParticipantsModal(eventId, eventTitle) {
    const modal = document.getElementById('participantsModal');
    const titleEl = document.getElementById('modalEventTitle');
    const metaEl = document.getElementById('modalEventMeta');
    const bodyEl = document.getElementById('modalParticipantsBody');

    titleEl.innerText = eventTitle;
    metaEl.innerText = `Event ID: #${eventId} — Registered Participants`;
    bodyEl.innerHTML = '<div class="loading-state">Fetching participants...</div>';
    modal.style.display = 'flex';

    try {
        const response = await fetch(`/api/events/${eventId}/participants`);
        if (!response.ok) throw new Error('Could not load participants');
        const participants = await response.json();

        if (participants.length === 0) {
            bodyEl.innerHTML = `
                <div class="empty-state">
                    <p>No students have registered for this event yet.</p>
                </div>`;
            return;
        }

        bodyEl.innerHTML = `
            <table class="participants-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Student Name</th>
                        <th>Roll Number</th>
                        <th>Email</th>
                    </tr>
                </thead>
                <tbody>
                    ${participants.map((p, idx) => `
                        <tr>
                            <td>${idx + 1}</td>
                            <td><strong>${escapeHtml(p.name)}</strong></td>
                            <td><code>${escapeHtml(p.rollNumber)}</code></td>
                            <td>${escapeHtml(p.email)}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (err) {
        bodyEl.innerHTML = `<div class="empty-state"><p style="color: var(--rose);">${err.message}</p></div>`;
    }
}

function closeParticipantsModal() {
    document.getElementById('participantsModal').style.display = 'none';
}

// ==========================================
// FEATURE 5: View & Cancel Registrations
// ==========================================
async function loadStudentRegistrations() {
    const studentId = document.getElementById('filterStudentSelect').value;
    const container = document.getElementById('studentRegistrationsContainer');

    if (!studentId) {
        container.innerHTML = `
            <div class="empty-state">
                <p>Select a student to view registrations.</p>
            </div>`;
        return;
    }

    container.innerHTML = '<div class="loading-state">Loading registrations...</div>';

    try {
        const response = await fetch(`/api/registrations/student/${studentId}`);
        if (!response.ok) throw new Error('Failed to load student registrations');
        const registrations = await response.json();

        if (registrations.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <p>This student currently has no registrations.</p>
                </div>`;
            return;
        }

        container.innerHTML = registrations.map(reg => {
            const isActive = reg.status === 'ACTIVE';
            const today = new Date().toISOString().split('T')[0];
            const canCancel = isActive && today < reg.eventDate;

            return `
                <div class="registration-item-card">
                    <div class="reg-info">
                        <h3>${escapeHtml(reg.eventTitle)}</h3>
                        <p><strong>Event Date:</strong> ${reg.eventDate} | <strong>Status:</strong> 
                            <span class="event-badge ${isActive ? 'badge-available' : 'badge-full'}">${reg.status}</span>
                        </p>
                        <p style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.25rem;">
                            Registered on: ${reg.registrationDate ? reg.registrationDate.replace('T', ' ').substring(0, 19) : 'N/A'}
                            ${reg.cancellationDate ? ` | Cancelled on: ${reg.cancellationDate.replace('T', ' ').substring(0, 19)}` : ''}
                        </p>
                    </div>
                    <div>
                        ${isActive ? `
                            <button class="btn btn-danger btn-sm" onclick="cancelRegistration(${reg.id}, ${studentId})" ${!canCancel ? 'title="Cannot cancel on or after event date" disabled' : ''}>
                                ${canCancel ? 'Cancel Registration' : 'Cannot Cancel (Date Passed/Today)'}
                            </button>
                        ` : `
                            <span style="color: var(--text-muted); font-size: 0.85rem; font-weight: 600;">Cancelled</span>
                        `}
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        container.innerHTML = `<div class="empty-state"><p style="color: var(--rose);">${err.message}</p></div>`;
    }
}

async function cancelRegistration(registrationId, studentId) {
    if (!confirm('Are you sure you want to cancel this registration? Your seat will be freed immediately.')) {
        return;
    }

    try {
        const response = await fetch(`/api/registrations/${registrationId}/cancel?studentId=${studentId}`, {
            method: 'PUT'
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Failed to cancel registration');
        }

        showToast('Registration cancelled. Seat is now available for other students.', 'success');
        await loadAllEvents();
        await loadStudentRegistrations();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================
// Students & Organizers Helpers
// ==========================================
async function loadStudents() {
    try {
        const response = await fetch('/api/students');
        if (!response.ok) return;
        allStudentsCache = await response.json();
        refreshRegistrationDropdowns();
        refreshStudentFilterDropdown();
    } catch (err) {
        console.error('Error loading students:', err);
    }
}

async function loadOrganizers() {
    try {
        const response = await fetch('/api/organizers');
        if (!response.ok) return;
        allOrganizersCache = await response.json();
        refreshOrganizerDropdown();
    } catch (err) {
        console.error('Error loading organizers:', err);
    }
}

function refreshRegistrationDropdowns() {
    const studentSelect = document.getElementById('regStudentSelect');
    if (studentSelect) {
        studentSelect.innerHTML = '<option value="">-- Choose a student --</option>' +
            allStudentsCache.map(s => `<option value="${s.id}">${escapeHtml(s.name)} (${s.rollNumber})</option>`).join('');
    }

    const eventSelect = document.getElementById('regEventSelect');
    if (eventSelect) {
        eventSelect.innerHTML = '<option value="">-- Choose an event --</option>' +
            allEventsCache.map(e => `<option value="${e.id}">${escapeHtml(e.title)} (${e.availableSeats} seats left)</option>`).join('');
    }
}

function refreshOrganizerDropdown() {
    const orgSelect = document.getElementById('eventOrganizerSelect');
    if (orgSelect) {
        orgSelect.innerHTML = '<option value="">-- Choose organizer --</option>' +
            allOrganizersCache.map(o => `<option value="${o.id}">${escapeHtml(o.name)} (${o.email})</option>`).join('');
    }
}

function refreshStudentFilterDropdown() {
    const filterSelect = document.getElementById('filterStudentSelect');
    if (filterSelect) {
        filterSelect.innerHTML = '<option value="">-- Select student --</option>' +
            allStudentsCache.map(s => `<option value="${s.id}">${escapeHtml(s.name)} (${s.rollNumber})</option>`).join('');
    }
}

async function handleCreateStudent(event) {
    event.preventDefault();
    const payload = {
        name: document.getElementById('studentName').value.trim(),
        email: document.getElementById('studentEmail').value.trim(),
        rollNumber: document.getElementById('studentRoll').value.trim()
    };

    try {
        const response = await fetch('/api/students', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Failed to create student');

        showToast(`Student "${data.name}" added successfully!`, 'success');
        document.getElementById('createStudentForm').reset();
        await loadStudents();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleCreateOrganizer(event) {
    event.preventDefault();
    const payload = {
        name: document.getElementById('orgName').value.trim(),
        email: document.getElementById('orgEmail').value.trim(),
        department: document.getElementById('orgDept').value.trim()
    };

    try {
        const response = await fetch('/api/organizers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Failed to create organizer');

        showToast(`Organizer "${data.name}" added successfully!`, 'success');
        document.getElementById('createOrganizerForm').reset();
        await loadOrganizers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';

    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
