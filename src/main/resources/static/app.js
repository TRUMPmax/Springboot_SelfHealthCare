const enumLabels = {
    gender: {
        MALE: "男",
        FEMALE: "女",
        OTHER: "其他"
    },
    riskLevel: {
        LOW: "低风险",
        MEDIUM: "中风险",
        HIGH: "高风险",
        CRITICAL: "极高风险"
    },
    alertSeverity: {
        LOW: "轻度",
        MEDIUM: "中度",
        HIGH: "重度",
        CRITICAL: "危急"
    },
    alertStatus: {
        PENDING: "待处理",
        REVIEWED: "已查看",
        RESOLVED: "已处置"
    },
    bloodType: {
        A: "A型",
        B: "B型",
        AB: "AB型",
        O: "O型",
        UNKNOWN: "未知"
    },
    smokingStatus: {
        NEVER: "从不吸烟",
        FORMER: "已戒烟",
        OCCASIONAL: "偶尔吸烟",
        CURRENT: "经常吸烟"
    },
    alcoholUseStatus: {
        NEVER: "不饮酒",
        OCCASIONAL: "偶尔饮酒",
        WEEKLY: "每周饮酒",
        FREQUENT: "频繁饮酒"
    }
};

const state = {
    profiles: [],
    profilesPage: 1,
    profilesPageSize: 10,
    records: [],
    recordsPage: 1,
    recordsPageSize: 10,
    alerts: [],
    alertsPage: 1,
    alertsPageSize: 10,
    dashboard: null,
    editingProfileId: null,
    editingRecordId: null,
    editingAlertId: null,
    activeProfileDetailId: null,
    activeProfileDetail: null,
    aiConsultation: null,
    aiLoading: false
};

const elements = {};
const modalIds = ["profileModal", "recordModal", "alertModal", "profileDetailModal"];

document.addEventListener("DOMContentLoaded", () => {
    cacheElements();
    applyStaticCopyOverrides();
    bindEvents();
    populateStaticOptions();
    resetProfileForm();
    resetRecordForm();
    loadAll().catch(handleError);
});

function cacheElements() {
    [
        "metricProfiles",
        "metricRecords",
        "metricPendingAlerts",
        "metricHighRisk",
        "riskDistribution",
        "recentRecordsBody",
        "recentAlertsFeed",
        "profileTableBody",
        "profilesPagination",
        "profilePageInfo",
        "profilePrevPage",
        "profileNextPage",
        "recordTableBody",
        "recordsPagination",
        "recordPageInfo",
        "recordPrevPage",
        "recordNextPage",
        "alertTableBody",
        "recordProfileFilter",
        "recordRiskFilter",
        "alertProfileFilter",
        "alertStatusFilter",
        "alertSeverityFilter",
        "alertsPagination",
        "alertPageInfo",
        "alertPrevPage",
        "alertNextPage",
        "recordProfileId",
        "profileModal",
        "recordModal",
        "alertModal",
        "profileDetailModal",
        "profileModalTitle",
        "recordModalTitle",
        "profileForm",
        "recordForm",
        "alertForm",
        "profileName",
        "profileRelationToUser",
        "profileGender",
        "profileAge",
        "profileBirthDate",
        "profileBloodType",
        "profilePhone",
        "profileEmail",
        "profileOccupation",
        "profileHeight",
        "profileWeight",
        "profileSmokingStatus",
        "profileAlcoholUseStatus",
        "profileEmergencyContact",
        "profileEmergencyContactPhone",
        "profileFamilyHistory",
        "profileChronicDiseases",
        "profileCurrentMedications",
        "profileSurgeryHistory",
        "profileAllergies",
        "profileExerciseHabit",
        "profileCareGoals",
        "profileNotes",
        "recordDate",
        "recordWeight",
        "recordWaistCircumference",
        "recordSystolic",
        "recordDiastolic",
        "recordHeartRate",
        "recordBloodSugar",
        "recordPostprandialBloodSugar",
        "recordTemperature",
        "recordOxygen",
        "recordCholesterol",
        "recordSleepHours",
        "recordExerciseMinutes",
        "recordStepsCount",
        "recordWaterIntakeMl",
        "recordStressLevel",
        "recordMoodScore",
        "recordSymptoms",
        "recordMedicationTaken",
        "recordNotes",
        "alertStatus",
        "alertHandledNote",
        "detailName",
        "detailSubtitle",
        "detailHeaderCards",
        "detailProfileGrid",
        "detailSuggestions",
        "trendGrid",
        "latestMetrics",
        "detailAlertsFeed",
        "detailRecordsBody",
        "aiQuestion",
        "triggerAiConsultation",
        "aiConsultationHint",
        "aiResponseBox",
        "toast"
    ].forEach((id) => {
        elements[id] = document.getElementById(id);
    });
}

function applyStaticCopyOverrides() {
    const aiSectionDescription = elements.triggerAiConsultation
        ?.closest(".sub-panel")
        ?.querySelector(".section-heading p");

    if (aiSectionDescription) {
        aiSectionDescription.textContent = "结合档案和近期记录生成分析建议。";
    }

    elements.triggerAiConsultation.textContent = "生成 AI 分析";
    elements.aiConsultationHint.textContent = "可补充具体问题。";
    elements.aiResponseBox.innerHTML = `<div class="empty-state">点击“生成 AI 分析”查看建议。</div>`;

    elements.profilePrevPage.textContent = "上一页";
    elements.profileNextPage.textContent = "下一页";
    elements.recordPrevPage.textContent = "上一页";
    elements.recordNextPage.textContent = "下一页";
    elements.alertPrevPage.textContent = "上一页";
    elements.alertNextPage.textContent = "下一页";
    elements.profilePageInfo.textContent = "第 1 / 1 页";
    elements.recordPageInfo.textContent = "第 1 / 1 页";
    elements.alertPageInfo.textContent = "第 1 / 1 页";
}

function bindEvents() {
    document.getElementById("quickCreateProfile").addEventListener("click", () => openProfileModal());
    document.getElementById("quickCreateRecord").addEventListener("click", () => openRecordModal());
    document.getElementById("openProfileModalBtn").addEventListener("click", () => openProfileModal());
    document.getElementById("openRecordModalBtn").addEventListener("click", () => openRecordModal());
    document.getElementById("refreshDashboard").addEventListener("click", () => loadDashboard().catch(handleError));
    document.getElementById("refreshAlertsBtn").addEventListener("click", () => {
        state.alertsPage = 1;
        loadAlerts().catch(handleError);
    });

    elements.profileForm.addEventListener("submit", handleProfileSubmit);
    elements.recordForm.addEventListener("submit", handleRecordSubmit);
    elements.alertForm.addEventListener("submit", handleAlertSubmit);
    elements.triggerAiConsultation.addEventListener("click", () => triggerAiConsultation().catch(handleError));

    elements.recordProfileFilter.addEventListener("change", () => resetRecordsPageAndReload());
    elements.recordRiskFilter.addEventListener("change", () => resetRecordsPageAndReload());
    elements.alertProfileFilter.addEventListener("change", () => resetAlertsPageAndReload());
    elements.alertStatusFilter.addEventListener("change", () => resetAlertsPageAndReload());
    elements.alertSeverityFilter.addEventListener("change", () => resetAlertsPageAndReload());
    elements.profilePrevPage.addEventListener("click", () => changeProfilesPage(-1));
    elements.profileNextPage.addEventListener("click", () => changeProfilesPage(1));
    elements.recordPrevPage.addEventListener("click", () => changeRecordsPage(-1));
    elements.recordNextPage.addEventListener("click", () => changeRecordsPage(1));
    elements.alertPrevPage.addEventListener("click", () => changeAlertsPage(-1));
    elements.alertNextPage.addEventListener("click", () => changeAlertsPage(1));

    document.addEventListener("click", handleActionClick);
    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") {
            return;
        }
        const activeModalId = [...modalIds].reverse().find((id) => !document.getElementById(id).hidden);
        if (activeModalId) {
            closeModal(activeModalId);
        }
    });

    document.querySelectorAll(".modal").forEach((modal) => {
        modal.addEventListener("click", (event) => {
            if (event.target === modal) {
                closeModal(modal.id);
            }
        });
    });
}

function populateStaticOptions() {
    setSelectOptions(elements.recordRiskFilter, [
        { value: "", label: "全部风险" },
        ...enumEntries(enumLabels.riskLevel)
    ]);

    setSelectOptions(elements.alertStatusFilter, [
        { value: "", label: "全部状态" },
        ...enumEntries(enumLabels.alertStatus)
    ]);

    setSelectOptions(elements.alertSeverityFilter, [
        { value: "", label: "全部级别" },
        ...enumEntries(enumLabels.alertSeverity)
    ]);

    setSelectOptions(elements.profileBloodType, [
        { value: "", label: "请选择血型" },
        ...enumEntries(enumLabels.bloodType)
    ]);

    setSelectOptions(elements.profileSmokingStatus, [
        { value: "", label: "请选择吸烟情况" },
        ...enumEntries(enumLabels.smokingStatus)
    ]);

    setSelectOptions(elements.profileAlcoholUseStatus, [
        { value: "", label: "请选择饮酒情况" },
        ...enumEntries(enumLabels.alcoholUseStatus)
    ]);
}

async function loadAll() {
    await Promise.all([loadDashboard(), loadProfiles()]);
    await Promise.all([loadRecords(), loadAlerts()]);
    await refreshOpenProfileDetail();
}

async function reloadData() {
    await Promise.all([loadDashboard(), loadProfiles(), loadRecords(), loadAlerts()]);
    await refreshOpenProfileDetail({ resetAi: true });
}

async function loadDashboard() {
    state.dashboard = await fetchJson("/api/dashboard");
    renderDashboard();
}

async function loadProfiles() {
    state.profiles = await fetchJson("/api/profiles");
    state.profilesPage = normalizePage(state.profilesPage, state.profiles.length, state.profilesPageSize);
    if (state.profiles.length === 0) {
        elements.profilesPagination.hidden = true;
    }
    renderProfiles();
    populateProfileOptions();

    if (state.activeProfileDetailId && !state.profiles.some((profile) => profile.id === state.activeProfileDetailId)) {
        state.activeProfileDetailId = null;
        state.activeProfileDetail = null;
        state.aiConsultation = null;
        closeModal("profileDetailModal");
    }
}

async function loadRecords() {
    const params = new URLSearchParams();
    if (elements.recordProfileFilter.value) {
        params.set("profileId", elements.recordProfileFilter.value);
    }
    if (elements.recordRiskFilter.value) {
        params.set("riskLevel", elements.recordRiskFilter.value);
    }

    const query = params.toString() ? `?${params.toString()}` : "";
    state.records = await fetchJson(`/api/records${query}`);
    state.recordsPage = normalizePage(state.recordsPage, state.records.length, state.recordsPageSize);
    if (state.records.length === 0) {
        elements.recordsPagination.hidden = true;
    }
    renderRecords();
}

async function loadAlerts() {
    const params = new URLSearchParams();
    if (elements.alertProfileFilter.value) {
        params.set("profileId", elements.alertProfileFilter.value);
    }
    if (elements.alertStatusFilter.value) {
        params.set("status", elements.alertStatusFilter.value);
    }
    if (elements.alertSeverityFilter.value) {
        params.set("severity", elements.alertSeverityFilter.value);
    }

    const query = params.toString() ? `?${params.toString()}` : "";
    state.alerts = await fetchJson(`/api/alerts${query}`);
    state.alertsPage = normalizePage(state.alertsPage, state.alerts.length, state.alertsPageSize);
    renderAlerts();
}

function populateProfileOptions() {
    const options = state.profiles.map((profile) => ({
        value: String(profile.id),
        label: `${profile.fullName}${profile.relationToUser ? ` · ${profile.relationToUser}` : ""}`
    }));

    const recordFilterValue = elements.recordProfileFilter.value;
    const alertFilterValue = elements.alertProfileFilter.value;
    const recordFormValue = elements.recordProfileId.value;

    setSelectOptions(elements.recordProfileFilter, [{ value: "", label: "全部档案" }, ...options], recordFilterValue);
    setSelectOptions(elements.alertProfileFilter, [{ value: "", label: "全部档案" }, ...options], alertFilterValue);
    setSelectOptions(
        elements.recordProfileId,
        options.length > 0 ? options : [{ value: "", label: "请先创建成员档案" }],
        recordFormValue || options[0]?.value || ""
    );
}

function renderDashboard() {
    const dashboard = state.dashboard;
    if (!dashboard) {
        return;
    }

    elements.metricProfiles.textContent = dashboard.totalProfiles ?? 0;
    elements.metricRecords.textContent = dashboard.totalRecords ?? 0;
    elements.metricPendingAlerts.textContent = dashboard.pendingAlerts ?? 0;
    elements.metricHighRisk.textContent = dashboard.highRiskRecords ?? 0;

    const totalRecords = dashboard.totalRecords || 0;
    elements.riskDistribution.innerHTML = ["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((level) => {
        const count = dashboard.riskDistribution?.[level] ?? 0;
        const width = totalRecords > 0 ? Math.max((count / totalRecords) * 100, count > 0 ? 8 : 0) : 0;

        return `
            <div class="risk-bar">
                <div class="risk-bar-top">
                    <strong>${escapeHtml(enumLabels.riskLevel[level])}</strong>
                    <span class="muted-text">${count} 条</span>
                </div>
                <div class="risk-track">
                    <span style="width:${width}%"></span>
                </div>
            </div>
        `;
    }).join("");

    if (!dashboard.recentRecords || dashboard.recentRecords.length === 0) {
        elements.recentRecordsBody.innerHTML = renderEmptyRow("暂无健康记录", 6);
    } else {
        elements.recentRecordsBody.innerHTML = dashboard.recentRecords.map((record) => `
            <tr>
                <td>${formatDate(record.recordDate)}</td>
                <td>${renderProfileLink(record.profile)}</td>
                <td>${formatBloodPressure(record.systolicPressure, record.diastolicPressure)}</td>
                <td>${formatBloodSugar(record.fastingBloodSugar, record.postprandialBloodSugar)}</td>
                <td>${formatMetric(record.bmi)}</td>
                <td>${renderBadge(record.riskLevel, "risk")}</td>
            </tr>
        `).join("");
    }

    if (!dashboard.recentAlerts || dashboard.recentAlerts.length === 0) {
        elements.recentAlertsFeed.innerHTML = `<div class="empty-state">暂无预警信息</div>`;
        return;
    }

    elements.recentAlertsFeed.innerHTML = dashboard.recentAlerts.map((alert) => `
        <article class="alert-feed-item">
            <div class="alert-feed-meta">
                ${renderBadge(alert.severity, "severity")}
                ${renderBadge(alert.status, "status")}
            </div>
            <h4>${escapeHtml(alert.title)}</h4>
            <div class="muted-text">${renderProfileInline(alert.profile)} · ${formatDate(alert.observedDate)}</div>
            <p>${escapeHtml(truncate(alert.suggestion, 92))}</p>
        </article>
    `).join("");
}

function renderProfiles() {
    if (state.profiles.length === 0) {
        elements.profileTableBody.innerHTML = renderEmptyRow("暂无成员档案，请先新增一条档案。", 6);
        return;
    }

    elements.profileTableBody.innerHTML = getCurrentProfilePageItems().map((profile) => `
        <tr>
            <td>
                <button class="table-link" type="button" data-action="view-profile" data-id="${profile.id}">
                    ${escapeHtml(profile.fullName)}
                </button>
                <div class="muted-text">${escapeHtml(profile.relationToUser || "未填写关系")}</div>
            </td>
            <td>${escapeHtml(formatProfileBasics(profile))}</td>
            <td>${escapeHtml(formatLifestyle(profile))}</td>
            <td>${escapeHtml(formatMedicalBackground(profile))}</td>
            <td>${escapeHtml(formatContact(profile))}</td>
            <td>
                <div class="cell-actions">
                    <button class="mini-btn" type="button" data-action="edit-profile" data-id="${profile.id}">编辑</button>
                    <button class="mini-btn danger" type="button" data-action="delete-profile" data-id="${profile.id}">删除</button>
                </div>
            </td>
        </tr>
    `).join("");

    renderProfilesPagination();
}

function renderRecords() {
    if (state.records.length === 0) {
        elements.recordTableBody.innerHTML = renderEmptyRow("暂无符合条件的健康记录。", 8);
        return;
    }

    elements.recordTableBody.innerHTML = getCurrentRecordPageItems().map((record) => `
        <tr>
            <td>${formatDate(record.recordDate)}</td>
            <td>${renderProfileLink(record.profile)}</td>
            <td>${formatBloodPressure(record.systolicPressure, record.diastolicPressure)}</td>
            <td>${formatBloodSugar(record.fastingBloodSugar, record.postprandialBloodSugar)}</td>
            <td>${formatWeightAndWaist(record)}</td>
            <td>${formatSleepAndExercise(record)}</td>
            <td>${renderBadge(record.riskLevel, "risk")}</td>
            <td>
                <div class="cell-actions">
                    <button class="mini-btn" type="button" data-action="edit-record" data-id="${record.id}">编辑</button>
                    <button class="mini-btn danger" type="button" data-action="delete-record" data-id="${record.id}">删除</button>
                </div>
            </td>
        </tr>
    `).join("");

    renderRecordsPagination();
}

function renderAlerts() {
    if (state.alerts.length === 0) {
        elements.alertTableBody.innerHTML = renderEmptyRow("暂无符合条件的预警记录。", 8);
        renderAlertsPagination();
        return;
    }

    elements.alertTableBody.innerHTML = getCurrentAlertPageItems().map((alert) => `
        <tr>
            <td>${formatDate(alert.observedDate)}</td>
            <td>${renderProfileLink(alert.profile)}</td>
            <td>${escapeHtml(alert.title)}</td>
            <td>${escapeHtml(alert.indicator || "-")}</td>
            <td>${renderBadge(alert.severity, "severity")}</td>
            <td>${renderBadge(alert.status, "status")}</td>
            <td>${escapeHtml(truncate(alert.suggestion, 72))}</td>
            <td>
                <div class="cell-actions">
                    <button class="mini-btn warning" type="button" data-action="edit-alert" data-id="${alert.id}">处理</button>
                    <button class="mini-btn danger" type="button" data-action="delete-alert" data-id="${alert.id}">删除</button>
                </div>
            </td>
        </tr>
    `).join("");

    renderAlertsPagination();
}

function resetAlertsPageAndReload() {
    state.alertsPage = 1;
    loadAlerts().catch(handleError);
}

function resetRecordsPageAndReload() {
    state.recordsPage = 1;
    loadRecords().catch(handleError);
}

function changeProfilesPage(offset) {
    const nextPage = state.profilesPage + offset;
    const totalPages = getProfilesTotalPages();
    if (nextPage < 1 || nextPage > totalPages) {
        return;
    }
    state.profilesPage = nextPage;
    renderProfiles();
}

function changeRecordsPage(offset) {
    const nextPage = state.recordsPage + offset;
    const totalPages = getRecordsTotalPages();
    if (nextPage < 1 || nextPage > totalPages) {
        return;
    }
    state.recordsPage = nextPage;
    renderRecords();
}

function changeAlertsPage(offset) {
    const nextPage = state.alertsPage + offset;
    const totalPages = getAlertsTotalPages();
    if (nextPage < 1 || nextPage > totalPages) {
        return;
    }
    state.alertsPage = nextPage;
    renderAlerts();
}

function getProfilesTotalPages() {
    return getTotalPages(state.profiles.length, state.profilesPageSize);
}

function getCurrentProfilePageItems() {
    return paginateItems(state.profiles, state.profilesPage, state.profilesPageSize);
}

function renderProfilesPagination() {
    renderPaginationBar({
        container: elements.profilesPagination,
        infoElement: elements.profilePageInfo,
        prevButton: elements.profilePrevPage,
        nextButton: elements.profileNextPage,
        page: state.profilesPage,
        totalItems: state.profiles.length,
        pageSize: state.profilesPageSize
    });
}

function getRecordsTotalPages() {
    return getTotalPages(state.records.length, state.recordsPageSize);
}

function getCurrentRecordPageItems() {
    return paginateItems(state.records, state.recordsPage, state.recordsPageSize);
}

function renderRecordsPagination() {
    renderPaginationBar({
        container: elements.recordsPagination,
        infoElement: elements.recordPageInfo,
        prevButton: elements.recordPrevPage,
        nextButton: elements.recordNextPage,
        page: state.recordsPage,
        totalItems: state.records.length,
        pageSize: state.recordsPageSize
    });
}

function getAlertsTotalPages() {
    return getTotalPages(state.alerts.length, state.alertsPageSize);
}

function getCurrentAlertPageItems() {
    return paginateItems(state.alerts, state.alertsPage, state.alertsPageSize);
}

function renderAlertsPagination() {
    renderPaginationBar({
        container: elements.alertsPagination,
        infoElement: elements.alertPageInfo,
        prevButton: elements.alertPrevPage,
        nextButton: elements.alertNextPage,
        page: state.alertsPage,
        totalItems: state.alerts.length,
        pageSize: state.alertsPageSize
    });
    return;

    const totalItems = state.alerts.length;
    const totalPages = getAlertsTotalPages();

    if (totalItems === 0) {
        elements.alertsPagination.hidden = true;
        elements.alertPageInfo.textContent = "第 1 页 / 共 1 页";
        elements.alertPrevPage.disabled = true;
        elements.alertNextPage.disabled = true;
        return;
    }

    const startIndex = (state.alertsPage - 1) * state.alertsPageSize + 1;
    const endIndex = Math.min(state.alertsPage * state.alertsPageSize, totalItems);

    elements.alertsPagination.hidden = false;
    elements.alertPageInfo.textContent =
        `第 ${state.alertsPage} 页 / 共 ${totalPages} 页 · 当前显示 ${startIndex}-${endIndex} 条，共 ${totalItems} 条`;
    elements.alertPrevPage.disabled = state.alertsPage <= 1;
    elements.alertNextPage.disabled = state.alertsPage >= totalPages;
}

async function openProfileDetail(profileId, options = {}) {
    state.activeProfileDetailId = profileId;
    state.aiConsultation = null;
    state.aiLoading = false;
    renderAiResponse();
    elements.aiQuestion.value = "";
    elements.aiConsultationHint.textContent = "仅在你主动点击按钮时才会调用 DeepSeek。";

    elements.aiConsultationHint.textContent = "可补充具体问题后再分析。";

    if (!options.keepOpen) {
        openModal("profileDetailModal");
    }

    renderDetailLoading();

    try {
        state.activeProfileDetail = await fetchJson(`/api/profiles/${profileId}/detail`);
        renderProfileDetail();
    } catch (error) {
        state.activeProfileDetail = null;
        closeModal("profileDetailModal");
        throw error;
    }
}

async function refreshOpenProfileDetail(options = {}) {
    if (!state.activeProfileDetailId || elements.profileDetailModal.hidden) {
        return;
    }

    const { resetAi = false } = options;
    if (resetAi) {
        state.aiConsultation = null;
        state.aiLoading = false;
        renderAiResponse();
    }

    state.activeProfileDetail = await fetchJson(`/api/profiles/${state.activeProfileDetailId}/detail`);
    renderProfileDetail();
}

function renderDetailLoading() {
    elements.detailName.textContent = "成员档案详情";
    elements.detailSubtitle.textContent = "正在加载该成员的趋势追踪与个性化建议...";
    elements.detailHeaderCards.innerHTML = `<div class="empty-state">正在加载概览信息...</div>`;
    elements.detailProfileGrid.innerHTML = `<div class="empty-state">正在加载档案信息...</div>`;
    elements.detailSuggestions.innerHTML = `<li class="empty-state">正在整理建议...</li>`;
    elements.trendGrid.innerHTML = `<div class="empty-state">正在计算趋势变化...</div>`;
    elements.latestMetrics.innerHTML = `<div class="empty-state">正在加载最新指标...</div>`;
    elements.detailAlertsFeed.innerHTML = `<div class="empty-state">正在加载预警信息...</div>`;
    elements.detailRecordsBody.innerHTML = renderEmptyRow("正在加载近期记录...", 6);
}

function renderProfileDetail() {
    const detail = state.activeProfileDetail;
    if (!detail) {
        return;
    }

    const profile = detail.profile;
    const latestRecord = detail.latestRecord;
    const recentAlerts = ensureArray(detail.recentAlerts);
    const recentRecords = ensureArray(detail.recentRecords);
    const trends = ensureArray(detail.trends);
    const suggestions = ensureArray(detail.personalizedSuggestions);

    elements.detailName.textContent = profile.fullName;
    elements.detailSubtitle.textContent = [
        profile.relationToUser || "家庭成员",
        enumLabels.gender[profile.gender] || profile.gender,
        profile.age ? `${profile.age}岁` : null
    ].filter(Boolean).join(" · ");

    elements.detailHeaderCards.innerHTML = [
        renderDetailCard(
            "当前风险",
            latestRecord?.riskLevel ? enumLabels.riskLevel[latestRecord.riskLevel] : "暂无评估",
            latestRecord ? `风险分 ${latestRecord.riskScore ?? "-"} · ${formatDate(latestRecord.recordDate)}` : "需要先录入健康记录"
        ),
        renderDetailCard(
            "趋势追踪",
            `${recentRecords.length} 条记录`,
            trends.length > 0 ? `已分析 ${trends.length} 项连续趋势` : "记录达到 2 条后更容易识别变化"
        ),
        renderDetailCard(
            "近期预警",
            `${recentAlerts.length} 条`,
            recentAlerts.length > 0 ? "可在下方查看异常指标与处理状态" : "当前暂无新的预警提醒"
        )
    ].join("");

    elements.detailProfileGrid.innerHTML = [
        infoItem("关系", profile.relationToUser),
        infoItem("性别", enumLabels.gender[profile.gender] || profile.gender),
        infoItem("年龄", profile.age ? `${profile.age}岁` : null),
        infoItem("出生日期", formatDate(profile.birthDate)),
        infoItem("血型", formatEnum(profile.bloodType, enumLabels.bloodType)),
        infoItem("职业", profile.occupation),
        infoItem("身高 / 体重", `${formatMetric(profile.heightCm, "cm")} / ${formatMetric(profile.weightKg, "kg")}`),
        infoItem("吸烟情况", formatEnum(profile.smokingStatus, enumLabels.smokingStatus)),
        infoItem("饮酒情况", formatEnum(profile.alcoholUseStatus, enumLabels.alcoholUseStatus)),
        infoItem("慢性病史", profile.chronicDiseases),
        infoItem("当前用药", profile.currentMedications),
        infoItem("手术史", profile.surgeryHistory),
        infoItem("过敏信息", profile.allergies),
        infoItem("家族病史", profile.familyHistory),
        infoItem("运动习惯", profile.exerciseHabit),
        infoItem("健康目标", profile.careGoals),
        infoItem("联系方式", profile.phone || profile.email ? [profile.phone, profile.email].filter(Boolean).join(" / ") : null),
        infoItem(
            "紧急联系人",
            profile.emergencyContact || profile.emergencyContactPhone
                ? [profile.emergencyContact, profile.emergencyContactPhone].filter(Boolean).join(" / ")
                : null
        ),
        infoItem("备注", profile.notes)
    ].join("");

    elements.detailSuggestions.innerHTML = suggestions.length > 0
        ? suggestions.map((item) => `<li>${escapeHtml(item)}</li>`).join("")
        : `<li>当前建议暂时不足，继续记录更多健康数据后会更准确。</li>`;

    elements.trendGrid.innerHTML = trends.length > 0
        ? trends.map((trend) => renderTrendCard(trend)).join("")
        : `<div class="empty-state">至少有 2 条记录后，系统会在这里展示趋势变化。</div>`;

    elements.latestMetrics.innerHTML = latestRecord
        ? renderLatestMetrics(latestRecord)
        : `<div class="empty-state">暂无最新健康记录。</div>`;

    elements.detailAlertsFeed.innerHTML = recentAlerts.length > 0
        ? recentAlerts.map((alert) => `
            <article class="alert-feed-item">
                <div class="alert-feed-meta">
                    ${renderBadge(alert.severity, "severity")}
                    ${renderBadge(alert.status, "status")}
                </div>
                <h4>${escapeHtml(alert.title)}</h4>
                <div class="muted-text">${formatDate(alert.observedDate)} · ${escapeHtml(alert.indicator || "系统综合判断")}</div>
                <p>${escapeHtml(alert.suggestion || "请结合近期记录持续观察。")}</p>
            </article>
        `).join("")
        : `<div class="empty-state">暂无预警信息。</div>`;

    elements.detailRecordsBody.innerHTML = recentRecords.length > 0
        ? recentRecords.map((record) => `
            <tr>
                <td>${formatDate(record.recordDate)}</td>
                <td>${formatBloodPressure(record.systolicPressure, record.diastolicPressure)}</td>
                <td>${formatBloodSugar(record.fastingBloodSugar, record.postprandialBloodSugar)}</td>
                <td>${formatMetric(record.sleepHours, "h")}</td>
                <td>${formatExerciseSummary(record)}</td>
                <td>${renderBadge(record.riskLevel, "risk")}</td>
            </tr>
        `).join("")
        : renderEmptyRow("暂无近期记录", 6);
}

function renderDetailCard(label, value, description) {
    return `
        <article class="detail-card">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(value)}</strong>
            <p>${escapeHtml(description)}</p>
        </article>
    `;
}

function renderTrendCard(trend) {
    const trendClass = directionClass(trend.direction);
    const latestValue = `${formatMetric(trend.latestValue, trend.unit || "")}`;
    const changeText = trend.previousValue == null
        ? "暂无上一次记录"
        : `${formatSignedMetric(trend.changeValue, trend.unit || "")} · 相比上一条`;

    return `
        <article class="trend-card ${trendClass}">
            <div class="trend-header">
                <div>
                    <strong>${escapeHtml(trend.metricName)}</strong>
                    <div class="muted-text">${escapeHtml(trend.interpretation || "暂无解释")}</div>
                </div>
                <span class="trend-chip ${trendClass}">${escapeHtml(trend.direction || "稳定")}</span>
            </div>
            <div class="trend-value">${escapeHtml(latestValue)}</div>
            <div class="muted-text">${escapeHtml(changeText)}</div>
            ${renderSparkline(trend.points, trend.metricName)}
        </article>
    `;
}

function renderLatestMetrics(record) {
    const metricItems = [
        ["血压", formatBloodPressure(record.systolicPressure, record.diastolicPressure)],
        ["空腹血糖", formatMetric(record.fastingBloodSugar, "mmol/L")],
        ["餐后血糖", formatMetric(record.postprandialBloodSugar, "mmol/L")],
        ["体重", formatMetric(record.weightKg, "kg")],
        ["腰围", formatMetric(record.waistCircumferenceCm, "cm")],
        ["BMI", formatMetric(record.bmi)],
        ["心率", formatMetric(record.heartRate, "次/分")],
        ["血氧", formatMetric(record.bloodOxygen, "%")],
        ["睡眠", formatMetric(record.sleepHours, "h")],
        ["运动", formatExerciseSummary(record)],
        ["饮水", formatMetric(record.waterIntakeMl, "ml")],
        ["情绪 / 压力", formatMoodStress(record)],
        ["症状", record.symptoms || "-"],
        ["用药记录", record.medicationTaken || "-"],
        ["系统摘要", record.summary || "-"],
        ["备注", record.notes || "-"]
    ];

    return metricItems.map(([label, value]) => `
        <div class="metric-kv">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(value)}</strong>
        </div>
    `).join("");
}

async function triggerAiConsultation() {
    if (!state.activeProfileDetailId) {
        showToast("请先打开某位成员的档案详情。", true);
        return;
    }

    state.aiLoading = true;
    renderAiResponse();
    elements.aiConsultationHint.textContent = "正在向 DeepSeek 请求分析，请稍候...";

    try {
        const response = await fetchJson(`/api/profiles/${state.activeProfileDetailId}/ai-consultation`, {
            method: "POST",
            body: JSON.stringify({
                focusRecordId: state.activeProfileDetail?.latestRecord?.id ?? null,
                question: stringOrNull(elements.aiQuestion.value)
            })
        });

        state.aiConsultation = response;
        elements.aiConsultationHint.textContent = "本次分析由你手动触发，系统不会自动反复调用 AI。";
        showToast("AI 健康分析已生成。");
    } finally {
        state.aiLoading = false;
        if (state.aiConsultation) {
            elements.aiConsultationHint.textContent = "分析已更新。";
        }
        renderAiResponse();
    }
}

function renderAiResponse() {
    if (state.aiLoading) {
        elements.aiResponseBox.innerHTML = `<div class="empty-state">AI 正在结合档案、趋势和近期记录生成建议...</div>`;
        return;
    }

    if (!state.aiConsultation) {
        elements.aiResponseBox.innerHTML = `<div class="empty-state">尚未发起 AI 咨询。</div>`;
        return;
    }

    elements.aiResponseBox.innerHTML = `
        <div class="ai-response-meta">
            <span>模型：${escapeHtml(state.aiConsultation.model || "-")}</span>
            <span>时间：${escapeHtml(formatDateTime(state.aiConsultation.generatedAt))}</span>
        </div>
        <div class="ai-response-body">${escapeMultiline(state.aiConsultation.answer)}</div>
        <div class="ai-response-footnote">上下文摘要：${escapeHtml(state.aiConsultation.contextDigest || "已基于当前成员档案生成")}</div>
    `;
}

async function triggerAiConsultation() {
    if (!state.activeProfileDetailId) {
        showToast("请先打开成员详情。", true);
        return;
    }

    state.aiLoading = true;
    elements.aiConsultationHint.textContent = "AI 正在生成分析...";
    renderAiResponse();

    try {
        const response = await fetchJson(`/api/profiles/${state.activeProfileDetailId}/ai-consultation`, {
            method: "POST",
            body: JSON.stringify({
                focusRecordId: state.activeProfileDetail?.latestRecord?.id ?? null,
                question: stringOrNull(elements.aiQuestion.value)
            })
        });

        state.aiConsultation = response;
        elements.aiConsultationHint.textContent = "分析已更新。";
        showToast("AI 健康分析已生成。");
    } finally {
        state.aiLoading = false;
        renderAiResponse();
    }
}

function renderAiResponse() {
    if (state.aiLoading) {
        elements.aiResponseBox.innerHTML = `<div class="empty-state">AI 正在生成分析...</div>`;
        return;
    }

    if (!state.aiConsultation) {
        elements.aiResponseBox.innerHTML = `<div class="empty-state">点击“生成 AI 分析”查看建议。</div>`;
        return;
    }

    elements.aiResponseBox.innerHTML = `
        <div class="ai-response-meta">
            <span>${escapeHtml(formatDateTime(state.aiConsultation.generatedAt))}</span>
        </div>
        <div class="ai-response-body">${escapeMultiline(state.aiConsultation.answer)}</div>
    `;
}

function handleActionClick(event) {
    const closeTrigger = event.target.closest("[data-close-modal]");
    if (closeTrigger) {
        closeModal(closeTrigger.dataset.closeModal);
        return;
    }

    const actionTrigger = event.target.closest("[data-action]");
    if (!actionTrigger) {
        return;
    }

    const id = Number(actionTrigger.dataset.id);
    const action = actionTrigger.dataset.action;

    if (action === "view-profile") {
        openProfileDetail(id).catch(handleError);
        return;
    }
    if (action === "edit-profile") {
        openProfileModal(id);
        return;
    }
    if (action === "delete-profile") {
        deleteProfile(id).catch(handleError);
        return;
    }
    if (action === "edit-record") {
        openRecordModal(id);
        return;
    }
    if (action === "delete-record") {
        deleteRecord(id).catch(handleError);
        return;
    }
    if (action === "edit-alert") {
        openAlertModal(id);
        return;
    }
    if (action === "delete-alert") {
        deleteAlert(id).catch(handleError);
    }
}

function openProfileModal(id) {
    state.editingProfileId = id ?? null;
    resetProfileForm();

    if (id) {
        const profile = state.profiles.find((item) => item.id === id);
        if (!profile) {
            showToast("未找到要编辑的成员档案。", true);
            return;
        }

        elements.profileModalTitle.textContent = "编辑成员档案";
        elements.profileName.value = profile.fullName || "";
        elements.profileRelationToUser.value = profile.relationToUser || "";
        elements.profileGender.value = profile.gender || "MALE";
        elements.profileAge.value = profile.age ?? "";
        elements.profileBirthDate.value = profile.birthDate || "";
        elements.profileBloodType.value = profile.bloodType || "";
        elements.profilePhone.value = profile.phone || "";
        elements.profileEmail.value = profile.email || "";
        elements.profileOccupation.value = profile.occupation || "";
        elements.profileHeight.value = profile.heightCm ?? "";
        elements.profileWeight.value = profile.weightKg ?? "";
        elements.profileSmokingStatus.value = profile.smokingStatus || "";
        elements.profileAlcoholUseStatus.value = profile.alcoholUseStatus || "";
        elements.profileEmergencyContact.value = profile.emergencyContact || "";
        elements.profileEmergencyContactPhone.value = profile.emergencyContactPhone || "";
        elements.profileFamilyHistory.value = profile.familyHistory || "";
        elements.profileChronicDiseases.value = profile.chronicDiseases || "";
        elements.profileCurrentMedications.value = profile.currentMedications || "";
        elements.profileSurgeryHistory.value = profile.surgeryHistory || "";
        elements.profileAllergies.value = profile.allergies || "";
        elements.profileExerciseHabit.value = profile.exerciseHabit || "";
        elements.profileCareGoals.value = profile.careGoals || "";
        elements.profileNotes.value = profile.notes || "";
    } else {
        elements.profileModalTitle.textContent = "新增成员档案";
    }

    openModal("profileModal");
}

function openRecordModal(id) {
    if (state.profiles.length === 0) {
        showToast("请先创建成员档案，再录入健康记录。", true);
        return;
    }

    state.editingRecordId = id ?? null;
    resetRecordForm();

    if (id) {
        const record = state.records.find((item) => item.id === id);
        if (!record) {
            showToast("未找到要编辑的健康记录。", true);
            return;
        }

        elements.recordModalTitle.textContent = "编辑健康记录";
        elements.recordProfileId.value = String(record.profile.id);
        elements.recordDate.value = record.recordDate || todayString();
        elements.recordWeight.value = record.weightKg ?? "";
        elements.recordWaistCircumference.value = record.waistCircumferenceCm ?? "";
        elements.recordSystolic.value = record.systolicPressure ?? "";
        elements.recordDiastolic.value = record.diastolicPressure ?? "";
        elements.recordHeartRate.value = record.heartRate ?? "";
        elements.recordBloodSugar.value = record.fastingBloodSugar ?? "";
        elements.recordPostprandialBloodSugar.value = record.postprandialBloodSugar ?? "";
        elements.recordTemperature.value = record.bodyTemperature ?? "";
        elements.recordOxygen.value = record.bloodOxygen ?? "";
        elements.recordCholesterol.value = record.cholesterolTotal ?? "";
        elements.recordSleepHours.value = record.sleepHours ?? "";
        elements.recordExerciseMinutes.value = record.exerciseMinutes ?? "";
        elements.recordStepsCount.value = record.stepsCount ?? "";
        elements.recordWaterIntakeMl.value = record.waterIntakeMl ?? "";
        elements.recordStressLevel.value = record.stressLevel ?? "";
        elements.recordMoodScore.value = record.moodScore ?? "";
        elements.recordSymptoms.value = record.symptoms || "";
        elements.recordMedicationTaken.value = record.medicationTaken || "";
        elements.recordNotes.value = record.notes || "";
    } else {
        elements.recordModalTitle.textContent = "新增健康记录";
    }

    openModal("recordModal");
}

function openAlertModal(id) {
    const alert = state.alerts.find((item) => item.id === id);
    if (!alert) {
        showToast("未找到要处理的预警信息。", true);
        return;
    }

    state.editingAlertId = id;
    elements.alertForm.reset();
    elements.alertStatus.value = alert.status || "PENDING";
    elements.alertHandledNote.value = alert.handledNote || "";
    openModal("alertModal");
}

function openModal(id) {
    document.body.classList.add("modal-open");
    document.getElementById(id).hidden = false;
}

function closeModal(id) {
    document.getElementById(id).hidden = true;
    if (id === "profileDetailModal") {
        state.aiLoading = false;
    }
    if (!modalIds.some((modalId) => !document.getElementById(modalId).hidden)) {
        document.body.classList.remove("modal-open");
    }
}

function resetProfileForm() {
    elements.profileForm.reset();
    elements.profileGender.value = "MALE";
    elements.profileBloodType.value = "";
    elements.profileSmokingStatus.value = "";
    elements.profileAlcoholUseStatus.value = "";
}

function resetRecordForm() {
    elements.recordForm.reset();
    elements.recordDate.value = todayString();
    if (state.profiles.length > 0) {
        elements.recordProfileId.value = String(state.profiles[0].id);
    }
}

async function handleProfileSubmit(event) {
    event.preventDefault();

    const payload = {
        fullName: stringOrNull(elements.profileName.value),
        relationToUser: stringOrNull(elements.profileRelationToUser.value),
        gender: elements.profileGender.value,
        age: integerOrNull(elements.profileAge.value),
        birthDate: stringOrNull(elements.profileBirthDate.value),
        bloodType: stringOrNull(elements.profileBloodType.value),
        phone: stringOrNull(elements.profilePhone.value),
        email: stringOrNull(elements.profileEmail.value),
        occupation: stringOrNull(elements.profileOccupation.value),
        heightCm: decimalOrNull(elements.profileHeight.value),
        weightKg: decimalOrNull(elements.profileWeight.value),
        smokingStatus: stringOrNull(elements.profileSmokingStatus.value),
        alcoholUseStatus: stringOrNull(elements.profileAlcoholUseStatus.value),
        familyHistory: stringOrNull(elements.profileFamilyHistory.value),
        chronicDiseases: stringOrNull(elements.profileChronicDiseases.value),
        allergies: stringOrNull(elements.profileAllergies.value),
        currentMedications: stringOrNull(elements.profileCurrentMedications.value),
        surgeryHistory: stringOrNull(elements.profileSurgeryHistory.value),
        exerciseHabit: stringOrNull(elements.profileExerciseHabit.value),
        careGoals: stringOrNull(elements.profileCareGoals.value),
        emergencyContact: stringOrNull(elements.profileEmergencyContact.value),
        emergencyContactPhone: stringOrNull(elements.profileEmergencyContactPhone.value),
        notes: stringOrNull(elements.profileNotes.value)
    };

    const url = state.editingProfileId ? `/api/profiles/${state.editingProfileId}` : "/api/profiles";
    const method = state.editingProfileId ? "PUT" : "POST";

    await fetchJson(url, {
        method,
        body: JSON.stringify(payload)
    });

    closeModal("profileModal");
    showToast(state.editingProfileId ? "成员档案已更新。" : "成员档案已创建。");
    await reloadData();
}

async function handleRecordSubmit(event) {
    event.preventDefault();

    const payload = {
        profileId: integerOrNull(elements.recordProfileId.value),
        recordDate: elements.recordDate.value,
        weightKg: decimalOrNull(elements.recordWeight.value),
        waistCircumferenceCm: decimalOrNull(elements.recordWaistCircumference.value),
        systolicPressure: integerOrNull(elements.recordSystolic.value),
        diastolicPressure: integerOrNull(elements.recordDiastolic.value),
        heartRate: integerOrNull(elements.recordHeartRate.value),
        fastingBloodSugar: decimalOrNull(elements.recordBloodSugar.value),
        postprandialBloodSugar: decimalOrNull(elements.recordPostprandialBloodSugar.value),
        bodyTemperature: decimalOrNull(elements.recordTemperature.value),
        bloodOxygen: decimalOrNull(elements.recordOxygen.value),
        cholesterolTotal: decimalOrNull(elements.recordCholesterol.value),
        sleepHours: decimalOrNull(elements.recordSleepHours.value),
        exerciseMinutes: integerOrNull(elements.recordExerciseMinutes.value),
        stepsCount: integerOrNull(elements.recordStepsCount.value),
        waterIntakeMl: integerOrNull(elements.recordWaterIntakeMl.value),
        stressLevel: integerOrNull(elements.recordStressLevel.value),
        moodScore: integerOrNull(elements.recordMoodScore.value),
        symptoms: stringOrNull(elements.recordSymptoms.value),
        medicationTaken: stringOrNull(elements.recordMedicationTaken.value),
        notes: stringOrNull(elements.recordNotes.value)
    };

    const url = state.editingRecordId ? `/api/records/${state.editingRecordId}` : "/api/records";
    const method = state.editingRecordId ? "PUT" : "POST";

    await fetchJson(url, {
        method,
        body: JSON.stringify(payload)
    });

    closeModal("recordModal");
    showToast(state.editingRecordId ? "健康记录已更新。" : "健康记录已保存，系统已重新评估风险。");
    await reloadData();
}

async function handleAlertSubmit(event) {
    event.preventDefault();

    await fetchJson(`/api/alerts/${state.editingAlertId}/status`, {
        method: "PUT",
        body: JSON.stringify({
            status: elements.alertStatus.value,
            handledNote: stringOrNull(elements.alertHandledNote.value)
        })
    });

    closeModal("alertModal");
    showToast("预警状态已更新。");
    await reloadData();
}

async function deleteProfile(id) {
    const profile = state.profiles.find((item) => item.id === id);
    if (!window.confirm(`确认删除成员“${profile?.fullName || id}”吗？该成员下的记录和预警也会一起删除。`)) {
        return;
    }

    await fetchJson(`/api/profiles/${id}`, { method: "DELETE" });
    if (state.activeProfileDetailId === id) {
        state.activeProfileDetailId = null;
        state.activeProfileDetail = null;
        state.aiConsultation = null;
        closeModal("profileDetailModal");
    }
    showToast("成员档案已删除。");
    await reloadData();
}

async function deleteRecord(id) {
    if (!window.confirm("确认删除这条健康记录吗？该记录关联的预警也会被移除。")) {
        return;
    }

    await fetchJson(`/api/records/${id}`, { method: "DELETE" });
    showToast("健康记录已删除。");
    await reloadData();
}

async function deleteAlert(id) {
    if (!window.confirm("确认删除这条预警信息吗？")) {
        return;
    }

    await fetchJson(`/api/alerts/${id}`, { method: "DELETE" });
    showToast("预警信息已删除。");
    await reloadData();
}

async function fetchJson(url, options = {}) {
    const config = {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...(options.headers || {})
        }
    };

    const response = await fetch(url, config);
    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const data = text ? safeJsonParse(text) : null;

    if (!response.ok) {
        const detailText = data?.details ? Object.values(data.details).join("；") : "";
        const message = data?.message || text || "请求失败";
        throw new Error(detailText ? `${message}：${detailText}` : message);
    }

    return data;
}

function setSelectOptions(select, options, value = "") {
    select.innerHTML = options.map((option) => `
        <option value="${escapeAttribute(option.value)}">${escapeHtml(option.label)}</option>
    `).join("");

    const normalizedValue = value == null ? "" : String(value);
    const exists = options.some((option) => option.value === normalizedValue);
    select.value = exists ? normalizedValue : options[0]?.value || "";
}

function getTotalPages(totalItems, pageSize) {
    return Math.max(1, Math.ceil(totalItems / pageSize));
}

function normalizePage(page, totalItems, pageSize) {
    return Math.min(Math.max(page, 1), getTotalPages(totalItems, pageSize));
}

function paginateItems(items, page, pageSize) {
    const startIndex = (page - 1) * pageSize;
    return items.slice(startIndex, startIndex + pageSize);
}

function renderPaginationBar({ container, infoElement, prevButton, nextButton, page, totalItems, pageSize }) {
    const totalPages = getTotalPages(totalItems, pageSize);
    const currentPage = normalizePage(page, totalItems, pageSize);

    if (totalItems <= pageSize) {
        container.hidden = true;
        infoElement.textContent = totalItems === 0 ? "第 1 / 1 页" : `共 ${totalItems} 条`;
        prevButton.disabled = true;
        nextButton.disabled = true;
        return;
    }

    const startIndex = (currentPage - 1) * pageSize + 1;
    const endIndex = Math.min(currentPage * pageSize, totalItems);

    container.hidden = false;
    infoElement.textContent = `第 ${currentPage} / ${totalPages} 页 · ${startIndex}-${endIndex} / ${totalItems} 条`;
    prevButton.disabled = currentPage <= 1;
    nextButton.disabled = currentPage >= totalPages;
}

function renderBadge(value, type) {
    if (!value) {
        return '<span class="badge low">-</span>';
    }

    const labels = type === "risk"
        ? enumLabels.riskLevel
        : type === "status"
            ? enumLabels.alertStatus
            : enumLabels.alertSeverity;

    const cssClass = type === "status"
        ? value === "PENDING"
            ? "high"
            : value === "REVIEWED"
                ? "medium"
                : "low"
        : value === "LOW"
            ? "low"
            : value === "MEDIUM"
                ? "medium"
                : value === "HIGH"
                    ? "high"
                    : "critical";

    return `<span class="badge ${cssClass}">${escapeHtml(labels[value] || value)}</span>`;
}

function renderEmptyRow(text, colspan) {
    return `<tr><td class="empty-state" colspan="${colspan}">${escapeHtml(text)}</td></tr>`;
}

function renderProfileLink(profile) {
    return `
        <button class="table-link" type="button" data-action="view-profile" data-id="${profile.id}">
            ${escapeHtml(profile.fullName)}
        </button>
        <div class="muted-text">${escapeHtml(profile.relationToUser || "家庭成员")}</div>
    `;
}

function renderProfileInline(profile) {
    return `<button class="table-link inline" type="button" data-action="view-profile" data-id="${profile.id}">${escapeHtml(profile.fullName)}</button>`;
}

function infoItem(label, value) {
    return `
        <div class="info-item">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(value || "-")}</strong>
        </div>
    `;
}

function renderSparkline(points, title) {
    const safePoints = ensureArray(points).filter((point) => point && point.value != null);
    if (safePoints.length === 0) {
        return `<div class="empty-state">暂无${escapeHtml(title)}趋势数据</div>`;
    }

    const width = 240;
    const height = 76;
    const padding = 8;
    const values = safePoints.map((point) => Number(point.value));
    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = max - min || 1;
    const step = safePoints.length > 1 ? (width - padding * 2) / (safePoints.length - 1) : 0;

    const path = safePoints.map((point, index) => {
        const x = padding + step * index;
        const y = height - padding - ((Number(point.value) - min) / range) * (height - padding * 2);
        return `${x},${y}`;
    }).join(" ");

    return `
        <svg class="sparkline" viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" aria-label="${escapeAttribute(title)}">
            <polyline points="${path}" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></polyline>
        </svg>
    `;
}

function formatProfileBasics(profile) {
    return [
        enumLabels.gender[profile.gender] || profile.gender,
        profile.age ? `${profile.age}岁` : null,
        profile.birthDate || null,
        formatEnum(profile.bloodType, enumLabels.bloodType)
    ].filter((item) => item && item !== "-").join(" / ") || "-";
}

function formatLifestyle(profile) {
    return [
        profile.occupation,
        formatEnum(profile.smokingStatus, enumLabels.smokingStatus),
        formatEnum(profile.alcoholUseStatus, enumLabels.alcoholUseStatus),
        truncate(profile.exerciseHabit, 24)
    ].filter((item) => item && item !== "-").join(" / ") || "-";
}

function formatMedicalBackground(profile) {
    return [
        truncate(profile.chronicDiseases, 18),
        truncate(profile.currentMedications, 18),
        truncate(profile.careGoals, 18)
    ].filter((item) => item && item !== "-").join(" / ") || "-";
}

function formatContact(profile) {
    return [
        profile.phone,
        profile.email,
        profile.emergencyContact ? `${profile.emergencyContact}${profile.emergencyContactPhone ? `(${profile.emergencyContactPhone})` : ""}` : null
    ].filter(Boolean).join(" / ") || "-";
}

function formatBloodPressure(systolic, diastolic) {
    if (systolic == null && diastolic == null) {
        return "-";
    }
    if (systolic == null) {
        return `${diastolic}`;
    }
    if (diastolic == null) {
        return `${systolic}`;
    }
    return `${systolic}/${diastolic} mmHg`;
}

function formatBloodSugar(fasting, postprandial) {
    if (fasting == null && postprandial == null) {
        return "-";
    }
    const fastingText = fasting == null ? "空腹 -" : `空腹 ${formatMetric(fasting, "mmol/L")}`;
    const postprandialText = postprandial == null ? "餐后 -" : `餐后 ${formatMetric(postprandial, "mmol/L")}`;
    return `${fastingText} / ${postprandialText}`;
}

function formatWeightAndWaist(record) {
    if (record.weightKg == null && record.waistCircumferenceCm == null) {
        return "-";
    }
    return `${formatMetric(record.weightKg, "kg")} / ${formatMetric(record.waistCircumferenceCm, "cm")}`;
}

function formatSleepAndExercise(record) {
    if (record.sleepHours == null && record.exerciseMinutes == null && record.stepsCount == null) {
        return "-";
    }
    return `${formatMetric(record.sleepHours, "h")} / ${formatExerciseSummary(record)}`;
}

function formatExerciseSummary(record) {
    if (record.exerciseMinutes == null && record.stepsCount == null) {
        return "-";
    }
    return [
        formatMetric(record.exerciseMinutes, "分钟"),
        formatMetric(record.stepsCount, "步")
    ].filter((item) => item !== "-").join(" · ") || "-";
}

function formatMoodStress(record) {
    if (record.moodScore == null && record.stressLevel == null) {
        return "-";
    }
    return `${record.moodScore ?? "-"} / ${record.stressLevel ?? "-"} 分`;
}

function formatMetric(value, unit = "") {
    if (value == null || value === "") {
        return "-";
    }
    return unit ? `${formatNumber(value)} ${unit}` : formatNumber(value);
}

function formatSignedMetric(value, unit = "") {
    if (value == null || value === "") {
        return "-";
    }
    const number = Number(value);
    const prefix = number > 0 ? "+" : "";
    return unit ? `${prefix}${formatNumber(number)} ${unit}` : `${prefix}${formatNumber(number)}`;
}

function formatNumber(value) {
    const number = Number(value);
    if (!Number.isFinite(number)) {
        return String(value);
    }
    return new Intl.NumberFormat("zh-CN", {
        maximumFractionDigits: 2
    }).format(number);
}

function formatDate(value) {
    return value ? String(value).slice(0, 10) : "-";
}

function formatDateTime(value) {
    return value ? String(value).replace("T", " ").slice(0, 16) : "-";
}

function formatEnum(value, labels) {
    return value ? labels[value] || value : "-";
}

function directionClass(direction) {
    if (direction === "上升") {
        return "trend-up";
    }
    if (direction === "下降") {
        return "trend-down";
    }
    return "trend-stable";
}

function enumEntries(labels) {
    return Object.entries(labels).map(([value, label]) => ({ value, label }));
}

function truncate(value, maxLength = 24) {
    if (!value) {
        return "-";
    }
    return value.length > maxLength ? `${value.slice(0, maxLength)}...` : value;
}

function stringOrNull(value) {
    const trimmed = value?.trim();
    return trimmed ? trimmed : null;
}

function integerOrNull(value) {
    if (value === "" || value == null) {
        return null;
    }
    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
}

function decimalOrNull(value) {
    if (value === "" || value == null) {
        return null;
    }
    const parsed = Number.parseFloat(value);
    return Number.isNaN(parsed) ? null : parsed;
}

function safeJsonParse(value) {
    try {
        return JSON.parse(value);
    } catch (error) {
        return null;
    }
}

function ensureArray(value) {
    return Array.isArray(value) ? value : [];
}

function todayString() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function handleError(error) {
    showToast(error.message || "操作失败，请稍后重试。", true);
}

let toastTimer = null;

function showToast(message, isError = false) {
    elements.toast.textContent = message;
    elements.toast.style.background = isError ? "rgba(159, 29, 29, 0.94)" : "rgba(27, 42, 40, 0.92)";
    elements.toast.classList.add("visible");
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => {
        elements.toast.classList.remove("visible");
    }, 2600);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function escapeAttribute(value) {
    return escapeHtml(value);
}

function escapeMultiline(value) {
    return escapeHtml(value || "-").replaceAll("\n", "<br>");
}
