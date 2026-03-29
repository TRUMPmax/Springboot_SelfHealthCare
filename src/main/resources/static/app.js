const enumLabels = {
    gender: { MALE: "男", FEMALE: "女", OTHER: "其他" },
    bloodType: { A: "A型", B: "B型", AB: "AB型", O: "O型", UNKNOWN: "未知" },
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
    },
    riskLevel: {
        LOW: "低风险",
        MEDIUM: "中风险",
        HIGH: "高风险",
        CRITICAL: "极高风险"
    },
    alertStatus: {
        PENDING: "待处理",
        REVIEWED: "已查看",
        RESOLVED: "已解决"
    },
    alertSeverity: {
        LOW: "轻度",
        MEDIUM: "中度",
        HIGH: "重度",
        CRITICAL: "危急"
    }
};

const routes = [
    { key: "dashboard", label: "总览" },
    { key: "profile", label: "个人档案" },
    { key: "records", label: "健康记录" },
    { key: "alerts", label: "预警中心" },
    { key: "imports", label: "文档导入" },
    { key: "visualization", label: "数据图表" },
    { key: "ai", label: "AI 分析" }
];

const routeMeta = {
    dashboard: { title: "健康总览", subtitle: "" },
    profile: { title: "个人档案", subtitle: "" },
    records: { title: "健康记录", subtitle: "" },
    alerts: { title: "预警中心", subtitle: "" },
    imports: { title: "文档导入", subtitle: "" },
    visualization: { title: "数据图表", subtitle: "" },
    ai: { title: "AI 分析", subtitle: "" }
};

const state = {
    session: null,
    authMode: "login",
    route: "dashboard",
    dashboard: null,
    profileDetail: {
        profile: null,
        latestRecord: null,
        trends: [],
        personalizedSuggestions: [],
        recentRecords: [],
        recentAlerts: []
    },
    records: {
        items: [],
        page: 1,
        size: 10,
        totalItems: 0,
        totalPages: 1,
        riskLevel: ""
    },
    alerts: {
        items: [],
        page: 1,
        size: 10,
        totalItems: 0,
        totalPages: 1,
        status: "",
        severity: ""
    },
    visualization: null,
    importResult: null,
    ai: {
        loading: false,
        result: null
    }
};

const elements = {};
let toastTimer = null;

document.addEventListener("DOMContentLoaded", () => {
    cacheElements();
    bindEvents();
    bootstrap().catch(handleError);
});

function cacheElements() {
    elements.app = document.getElementById("app");
    elements.toast = document.getElementById("toast");
    elements.modalBackdrop = document.getElementById("modalBackdrop");
    elements.modalPanel = document.getElementById("modalPanel");
}

function bindEvents() {
    document.addEventListener("click", handleClick);
    document.addEventListener("submit", handleSubmit);
    document.addEventListener("change", handleChange);
    window.addEventListener("hashchange", handleHashRoute);
    elements.modalBackdrop.addEventListener("click", (event) => {
        if (event.target === elements.modalBackdrop) {
            closeModal();
        }
    });
}

async function bootstrap() {
    await restoreSession();
    render();
}

async function restoreSession() {
    const session = await api("/api/auth/me", { suppressUnauthorizedRedirect: true });
    if (session?.authenticated) {
        state.session = session;
        syncRoute();
        await loadAllData();
    } else {
        state.session = null;
        state.route = "dashboard";
    }
}

function handleHashRoute() {
    if (!state.session) {
        return;
    }
    syncRoute();
    render();
}

function syncRoute() {
    const rawHash = window.location.hash.replace("#", "");
    state.route = routes.some((item) => item.key === rawHash) ? rawHash : "dashboard";
    if (window.location.hash !== `#${state.route}`) {
        history.replaceState(null, "", `#${state.route}`);
    }
}

async function loadAllData() {
    await Promise.all([
        loadDashboard(),
        loadProfileDetail(),
        loadRecords(),
        loadAlerts(),
        loadVisualization()
    ]);
}

async function loadDashboard() {
    state.dashboard = await api("/api/dashboard");
}

async function loadProfileDetail() {
    state.profileDetail = await api("/api/profile/detail");
}

async function loadRecords() {
    const params = new URLSearchParams({
        page: String(state.records.page),
        size: String(state.records.size)
    });
    if (state.records.riskLevel) {
        params.set("riskLevel", state.records.riskLevel);
    }
    const data = await api(`/api/records?${params.toString()}`);
    Object.assign(state.records, data);
}

async function loadAlerts() {
    const params = new URLSearchParams({
        page: String(state.alerts.page),
        size: String(state.alerts.size)
    });
    if (state.alerts.status) {
        params.set("status", state.alerts.status);
    }
    if (state.alerts.severity) {
        params.set("severity", state.alerts.severity);
    }
    const data = await api(`/api/alerts?${params.toString()}`);
    Object.assign(state.alerts, data);
}

async function loadVisualization() {
    state.visualization = await api("/api/visualization");
}

function render() {
    closeModal();
    if (!state.session) {
        document.body.classList.remove("modal-open");
        elements.app.innerHTML = renderAuthPage();
        return;
    }

    const meta = routeMeta[state.route];
    elements.app.innerHTML = `
        <div class="app-shell">
            <aside class="sidebar">
                <div class="brand">
                    <strong>个人健康平台</strong>
                    <span>${escapeHtml(state.session.displayName)}</span>
                </div>
                <nav class="nav-list">
                    ${routes.map((item) => `
                        <button class="nav-item ${item.key === state.route ? "active" : ""}" type="button"
                                data-action="navigate" data-route="${item.key}">
                            <span>${escapeHtml(item.label)}</span>
                        </button>
                    `).join("")}
                </nav>
                <div style="margin-top:16px;">
                    <button class="btn-ghost" type="button" data-action="logout">退出登录</button>
                </div>
            </aside>
            <main class="app-main">
                <div class="topbar">
                    <div>
                        <h2>${escapeHtml(meta.title)}</h2>
                        ${meta.subtitle ? `<p>${escapeHtml(meta.subtitle)}</p>` : ""}
                    </div>
                    <div class="muted-text">当前用户：${escapeHtml(state.session.username)}</div>
                </div>
                ${renderCurrentPage()}
            </main>
        </div>
    `;
}

function renderAuthPage() {
    return `
        <div class="auth-shell">
            <div class="auth-card compact">
                <section class="auth-panel">
                    <h1 class="auth-title">个人健康管理</h1>
                    <div class="auth-tabs">
                        <button class="tab-btn ${state.authMode === "login" ? "active" : ""}" type="button"
                                data-action="auth-tab" data-mode="login">登录</button>
                        <button class="tab-btn ${state.authMode === "register" ? "active" : ""}" type="button"
                                data-action="auth-tab" data-mode="register">注册</button>
                    </div>
                    ${state.authMode === "login" ? renderLoginForm() : renderRegisterForm()}
                </section>
            </div>
        </div>
    `;
}

function renderLoginForm() {
    return `
        <form id="loginForm" class="page-grid">
            <label class="form-field">
                用户名
                <input name="username" type="text" maxlength="40" required>
            </label>
            <label class="form-field">
                密码
                <input name="password" type="password" maxlength="64" required>
            </label>
            <button class="btn" type="submit">登录系统</button>
        </form>
    `;
}

function renderRegisterForm() {
    return `
        <form id="registerForm" class="page-grid">
            <label class="form-field">
                用户名
                <input name="username" type="text" maxlength="40" required>
            </label>
            <label class="form-field">
                显示名称
                <input name="displayName" type="text" maxlength="60" required>
            </label>
            <label class="form-field">
                密码
                <input name="password" type="password" maxlength="64" required>
            </label>
            <label class="form-field">
                确认密码
                <input name="confirmPassword" type="password" maxlength="64" required>
            </label>
            <button class="btn" type="submit">注册并进入</button>
        </form>
    `;
}

function renderCurrentPage() {
    switch (state.route) {
        case "dashboard":
            return renderDashboardPage();
        case "profile":
            return renderProfilePage();
        case "records":
            return renderRecordsPage();
        case "alerts":
            return renderAlertsPage();
        case "imports":
            return renderImportsPage();
        case "visualization":
            return renderVisualizationPage();
        case "ai":
            return renderAiPage();
        default:
            return "";
    }
}

function renderDashboardPage() {
    const dashboard = state.dashboard;
    if (!dashboard) {
        return renderEmpty("正在加载总览数据。");
    }

    const total = dashboard.totalRecords || 0;
    return `
        <div class="page-grid">
            <div class="stat-grid">
                <div class="stat-card">
                    <span>档案完整度</span>
                    <strong>${dashboard.profileCompletionScore ?? 0}%</strong>
                </div>
                <div class="stat-card">
                    <span>健康记录</span>
                    <strong>${dashboard.totalRecords ?? 0}</strong>
                </div>
                <div class="stat-card">
                    <span>待处理预警</span>
                    <strong>${dashboard.pendingAlerts ?? 0}</strong>
                </div>
                <div class="stat-card">
                    <span>最近风险等级</span>
                    <strong>${dashboard.latestRiskLevel ? enumLabels.riskLevel[dashboard.latestRiskLevel] : "暂无"}</strong>
                </div>
            </div>

            ${!dashboard.profileExists ? `<div class="callout">请先完善个人档案。</div>` : ""}

            <section class="panel">
                <div class="section-head">
                    <h3>风险分布</h3>
                    <button class="btn-ghost" type="button" data-action="refresh-dashboard">刷新</button>
                </div>
                <div class="risk-bars">
                    ${["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((level) => {
                        const count = dashboard.riskDistribution?.[level] ?? 0;
                        const width = total > 0 ? Math.max((count / total) * 100, count > 0 ? 6 : 0) : 0;
                        return `
                            <div class="risk-bar">
                                <div class="risk-bar-head">
                                    <strong>${enumLabels.riskLevel[level]}</strong>
                                    <span class="muted-text">${count} 条</span>
                                </div>
                                <div class="risk-track"><span style="width:${width}%"></span></div>
                            </div>
                        `;
                    }).join("")}
                </div>
            </section>

            <div class="two-column">
                <section class="table-shell">
                    <div class="section-head"><h3>近期记录</h3></div>
                    ${renderRecordsTable(dashboard.recentRecords || [], false)}
                </section>
                <section class="panel">
                    <div class="section-head"><h3>近期预警</h3></div>
                    ${renderAlertFeed(dashboard.recentAlerts || [])}
                </section>
            </div>
        </div>
    `;
}

function renderProfilePage() {
    const detail = state.profileDetail;
    const profile = detail.profile || {};
    return `
        <div class="page-grid">
            <div class="two-column">
                <section class="panel">
                    <div class="section-head">
                        <h3>个人档案维护</h3>
                        <span class="badge ${profile.completionScore >= 80 ? "low" : profile.completionScore >= 50 ? "medium" : "high"}">
                            完整度 ${profile.completionScore ?? 0}%
                        </span>
                    </div>
                    <form id="profileForm" class="form-grid">
                        ${renderProfileFields(profile)}
                        <div class="full-span">
                            <button class="btn" type="submit">保存档案</button>
                        </div>
                    </form>
                </section>

                <section class="panel">
                    <div class="section-head"><h3>档案洞察</h3></div>
                    <div class="detail-grid">
                        <div class="card">
                            <span class="muted-text">最近记录日期</span>
                            <strong>${formatDate(detail.latestRecord?.recordDate)}</strong>
                        </div>
                        <div class="card">
                            <span class="muted-text">当前风险等级</span>
                            <strong>${detail.latestRecord?.riskLevel ? enumLabels.riskLevel[detail.latestRecord.riskLevel] : "暂无"}</strong>
                        </div>
                    </div>
                    <div class="suggestion-list" style="margin-top:16px;">
                        ${detail.personalizedSuggestions?.length
                            ? detail.personalizedSuggestions.map((item) => `<div class="feed-item">${escapeHtml(item)}</div>`).join("")
                            : `<div class="empty-shell">暂无个性化建议。</div>`}
                    </div>
                </section>
            </div>

            <section class="panel">
                <div class="section-head"><h3>近期趋势摘要</h3></div>
                <div class="three-column">
                    ${detail.trends?.length
                        ? detail.trends.map((trend) => `
                            <div class="card">
                                <div class="section-head">
                                    <div>
                                        <h4>${escapeHtml(trend.metricName)}</h4>
                                        <p>${escapeHtml(trend.direction)}</p>
                                    </div>
                                    <span class="badge ${trend.direction === "上升" ? "medium" : "low"}">${escapeHtml(trend.direction)}</span>
                                </div>
                                <strong>${formatMetric(trend.latestValue, trend.unit)}</strong>
                                <div class="muted-text">较前次 ${formatSignedMetric(trend.changeValue, trend.unit)}</div>
                                <div class="muted-text" style="margin-top:8px;">${escapeHtml(trend.interpretation)}</div>
                            </div>
                        `).join("")
                        : `<div class="empty-shell full-span">暂无趋势数据，请先录入健康记录。</div>`}
                </div>
            </section>
        </div>
    `;
}

function renderProfileFields(profile) {
    return `
        <label class="form-field">
            姓名
            <input name="fullName" type="text" value="${escapeAttr(profile.fullName || "")}" required>
        </label>
        <label class="form-field">
            性别
            <select name="gender">
                <option value="">请选择</option>
                ${renderSelectOptions(enumLabels.gender, profile.gender || "")}
            </select>
        </label>
        <label class="form-field">
            年龄
            <input name="age" type="number" min="1" max="120" value="${escapeAttr(profile.age ?? "")}">
        </label>
        <label class="form-field">
            出生日期
            <input name="birthDate" type="date" value="${escapeAttr(profile.birthDate || "")}">
        </label>
        <label class="form-field">
            血型
            <select name="bloodType">
                <option value="">请选择</option>
                ${renderSelectOptions(enumLabels.bloodType, profile.bloodType || "")}
            </select>
        </label>
        <label class="form-field">
            手机号
            <input name="phone" type="text" value="${escapeAttr(profile.phone || "")}">
        </label>
        <label class="form-field">
            邮箱
            <input name="email" type="email" value="${escapeAttr(profile.email || "")}">
        </label>
        <label class="form-field">
            职业
            <input name="occupation" type="text" value="${escapeAttr(profile.occupation || "")}">
        </label>
        <label class="form-field">
            身高(cm)
            <input name="heightCm" type="number" step="0.01" min="50" max="250" value="${escapeAttr(profile.heightCm ?? "")}">
        </label>
        <label class="form-field">
            体重(kg)
            <input name="weightKg" type="number" step="0.01" min="20" max="300" value="${escapeAttr(profile.weightKg ?? "")}">
        </label>
        <label class="form-field">
            吸烟情况
            <select name="smokingStatus">
                <option value="">请选择</option>
                ${renderSelectOptions(enumLabels.smokingStatus, profile.smokingStatus || "")}
            </select>
        </label>
        <label class="form-field">
            饮酒情况
            <select name="alcoholUseStatus">
                <option value="">请选择</option>
                ${renderSelectOptions(enumLabels.alcoholUseStatus, profile.alcoholUseStatus || "")}
            </select>
        </label>
        <label class="form-field full-span"><span>家族病史</span><textarea name="familyHistory">${escapeHtml(profile.familyHistory || "")}</textarea></label>
        <label class="form-field full-span"><span>慢性病史</span><textarea name="chronicDiseases">${escapeHtml(profile.chronicDiseases || "")}</textarea></label>
        <label class="form-field full-span"><span>过敏信息</span><textarea name="allergies">${escapeHtml(profile.allergies || "")}</textarea></label>
        <label class="form-field full-span"><span>当前用药</span><textarea name="currentMedications">${escapeHtml(profile.currentMedications || "")}</textarea></label>
        <label class="form-field full-span"><span>手术史</span><textarea name="surgeryHistory">${escapeHtml(profile.surgeryHistory || "")}</textarea></label>
        <label class="form-field full-span"><span>运动习惯</span><textarea name="exerciseHabit">${escapeHtml(profile.exerciseHabit || "")}</textarea></label>
        <label class="form-field full-span"><span>健康目标</span><textarea name="careGoals">${escapeHtml(profile.careGoals || "")}</textarea></label>
        <label class="form-field">
            紧急联系人
            <input name="emergencyContact" type="text" value="${escapeAttr(profile.emergencyContact || "")}">
        </label>
        <label class="form-field">
            紧急联系人电话
            <input name="emergencyContactPhone" type="text" value="${escapeAttr(profile.emergencyContactPhone || "")}">
        </label>
        <label class="form-field full-span"><span>备注</span><textarea name="notes">${escapeHtml(profile.notes || "")}</textarea></label>
    `;
}

function renderRecordsPage() {
    return `
        <div class="page-grid">
            <section class="table-shell">
                <div class="section-head">
                    <h3>健康记录列表</h3>
                    <button class="btn" type="button" data-action="open-record-create">新增记录</button>
                </div>
                <div class="toolbar">
                    <label>
                        风险筛选
                        <select id="recordRiskFilter">
                            <option value="">全部风险</option>
                            ${renderSelectOptions(enumLabels.riskLevel, state.records.riskLevel)}
                        </select>
                    </label>
                </div>
                ${renderRecordsTable(state.records.items, true)}
                ${renderPagination(state.records, "records")}
            </section>
        </div>
    `;
}

function renderAlertsPage() {
    return `
        <div class="page-grid">
            <section class="table-shell">
                <div class="section-head"><h3>预警列表</h3></div>
                <div class="toolbar">
                    <label>
                        处理状态
                        <select id="alertStatusFilter">
                            <option value="">全部状态</option>
                            ${renderSelectOptions(enumLabels.alertStatus, state.alerts.status)}
                        </select>
                    </label>
                    <label>
                        严重级别
                        <select id="alertSeverityFilter">
                            <option value="">全部级别</option>
                            ${renderSelectOptions(enumLabels.alertSeverity, state.alerts.severity)}
                        </select>
                    </label>
                </div>
                ${renderAlertsTable(state.alerts.items)}
                ${renderPagination(state.alerts, "alerts")}
            </section>
        </div>
    `;
}

function renderImportsPage() {
    return `
        <div class="page-grid">
            <section class="panel">
                <div class="section-head"><h3>导入健康文档</h3></div>
                <form id="importForm" class="page-grid">
                    <div class="import-dropzone">
                        <label class="form-field">
                            选择文件
                            <input name="file" type="file" accept=".pdf,.png,.jpg,.jpeg,.bmp,.txt" required>
                        </label>
                        <button class="btn" type="submit">开始识别并入档</button>
                    </div>
                </form>
            </section>
            ${renderImportResult()}
        </div>
    `;
}

function renderImportResult() {
    const result = state.importResult;
    if (!result) {
        return renderEmpty("导入结果会显示在这里。");
    }

    return `
        <section class="panel">
            <div class="section-head"><h3>最近一次导入结果</h3></div>
            <div class="import-result-grid">
                <div class="callout">${escapeHtml(result.disclaimer)}</div>
                <div class="muted-text">${escapeHtml(result.fileName || "未命名文件")} · ${escapeHtml(result.extractionMethod)}</div>
                <div class="chip-list">
                    ${(result.matchedFields || []).map((field) => `<span class="chip">${escapeHtml(field)}</span>`).join("") || "<span class='muted-text'>暂无匹配字段</span>"}
                </div>
                ${(result.warnings || []).length ? `
                    <div class="callout danger">
                        ${(result.warnings || []).map((item) => `<div>${escapeHtml(item)}</div>`).join("")}
                    </div>
                ` : ""}
                <div class="two-column">
                    <div class="card">
                        <span class="muted-text">档案姓名</span>
                        <strong>${escapeHtml(result.profile?.fullName || "-")}</strong>
                    </div>
                    <div class="card">
                        <span class="muted-text">是否生成记录</span>
                        <strong>${result.archivedRecord ? "已生成" : "未生成"}</strong>
                    </div>
                </div>
                <div class="card">
                    <div class="section-head"><h4>识别文本预览</h4></div>
                    <div class="ai-answer">${escapeHtml(result.extractedTextPreview || "")}</div>
                </div>
            </div>
        </section>
    `;
}

function renderVisualizationPage() {
    const visualization = state.visualization;
    if (!visualization) {
        return renderEmpty("正在加载图表数据。");
    }

    return `
        <div class="page-grid">
            <section class="panel">
                <div class="section-head">
                    <h3>指标趋势图</h3>
                    <button class="btn-ghost" type="button" data-action="refresh-visualization">刷新图表</button>
                </div>
                <div class="chart-grid">
                    ${(visualization.series || []).length
                        ? visualization.series.map((series) => renderChartCard(series)).join("")
                        : `<div class="empty-shell full-span">暂无可视化数据，请先录入或导入健康记录。</div>`}
                </div>
            </section>
            <section class="table-shell">
                <div class="section-head"><h3>最近 10 条记录</h3></div>
                ${renderRecordsTable(visualization.latestRecords || [], false)}
            </section>
        </div>
    `;
}

function renderAiPage() {
    const detail = state.profileDetail;
    return `
        <div class="page-grid">
            <div class="callout danger">
                AI 分析仅作为健康管理参考，不能替代医生诊断与治疗建议。若出现身体不适症状，请立即就医。
            </div>
            <section class="panel">
                <div class="section-head"><h3>发起 AI 分析</h3></div>
                <form id="aiForm" class="page-grid">
                    <label class="form-field">
                        关联记录
                        <select name="focusRecordId">
                            <option value="">默认使用最近一次记录</option>
                            ${(detail.recentRecords || []).map((record) => `
                                <option value="${record.id}">
                                    ${formatDate(record.recordDate)} · ${enumLabels.riskLevel[record.riskLevel] || "未评估"}
                                </option>
                            `).join("")}
                        </select>
                    </label>
                    <label class="form-field full-span">
                        具体问题
                        <textarea name="question" placeholder="例如：最近血糖和睡眠同时波动，下一步应该重点关注什么？"></textarea>
                    </label>
                    <div class="full-span">
                        <button class="btn" type="submit" ${state.ai.loading ? "disabled" : ""}>
                            ${state.ai.loading ? "分析中..." : "开始 AI 分析"}
                        </button>
                    </div>
                </form>
            </section>
            <section class="panel">
                <div class="section-head"><h3>分析结果</h3></div>
                ${state.ai.result ? `
                    <div class="page-grid">
                        <div class="muted-text">模型：${escapeHtml(state.ai.result.model || "-")}</div>
                        <div class="ai-answer">${escapeHtml(state.ai.result.answer || "")}</div>
                        <div class="callout danger">${escapeHtml(state.ai.result.disclaimer || "")}</div>
                    </div>
                ` : `<div class="empty-shell">尚未发起 AI 分析。</div>`}
            </section>
        </div>
    `;
}

function renderRecordsTable(records, withActions) {
    if (!records || records.length === 0) {
        return renderEmpty("暂无健康记录。");
    }

    return `
        <div class="table-wrapper">
            <table>
                <thead>
                <tr>
                    <th>日期</th>
                    <th>血压</th>
                    <th>血糖</th>
                    <th>体重 / BMI</th>
                    <th>睡眠 / 运动</th>
                    <th>风险</th>
                    ${withActions ? "<th>操作</th>" : ""}
                </tr>
                </thead>
                <tbody>
                ${records.map((record) => `
                    <tr>
                        <td>${formatDate(record.recordDate)}</td>
                        <td>${formatBloodPressure(record.systolicPressure, record.diastolicPressure)}</td>
                        <td>${formatBloodSugar(record.fastingBloodSugar, record.postprandialBloodSugar)}</td>
                        <td>${formatMetric(record.weightKg, "kg")} / ${formatMetric(record.bmi)}</td>
                        <td>${formatMetric(record.sleepHours, "h")} / ${formatMetric(record.exerciseMinutes, "min")}</td>
                        <td>${renderBadge(record.riskLevel, "risk")}</td>
                        ${withActions ? `
                            <td>
                                <div class="table-actions">
                                    <button class="mini-btn" type="button" data-action="open-record-edit" data-id="${record.id}">编辑</button>
                                    <button class="mini-btn danger" type="button" data-action="delete-record" data-id="${record.id}">删除</button>
                                </div>
                            </td>
                        ` : ""}
                    </tr>
                `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderAlertsTable(alerts) {
    if (!alerts || alerts.length === 0) {
        return renderEmpty("暂无预警信息。");
    }

    return `
        <div class="table-wrapper">
            <table>
                <thead>
                <tr>
                    <th>日期</th>
                    <th>标题</th>
                    <th>指标说明</th>
                    <th>严重级别</th>
                    <th>处理状态</th>
                    <th>建议</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                ${alerts.map((alert) => `
                    <tr>
                        <td>${formatDate(alert.observedDate)}</td>
                        <td>${escapeHtml(alert.title)}</td>
                        <td>${escapeHtml(alert.indicator || "-")}</td>
                        <td>${renderBadge(alert.severity, "severity")}</td>
                        <td>${renderBadge(alert.status, "status")}</td>
                        <td>${escapeHtml(truncate(alert.suggestion, 64))}</td>
                        <td>
                            <div class="table-actions">
                                <button class="mini-btn warning" type="button" data-action="open-alert-status" data-id="${alert.id}">处理</button>
                                <button class="mini-btn danger" type="button" data-action="delete-alert" data-id="${alert.id}">删除</button>
                            </div>
                        </td>
                    </tr>
                `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderAlertFeed(alerts) {
    if (!alerts || alerts.length === 0) {
        return renderEmpty("暂无预警数据。");
    }
    return `
        <div class="feed-list">
            ${alerts.map((alert) => `
                <div class="feed-item">
                    <div class="table-actions">
                        ${renderBadge(alert.severity, "severity")}
                        ${renderBadge(alert.status, "status")}
                    </div>
                    <h4>${escapeHtml(alert.title)}</h4>
                    <div class="muted-text">${formatDate(alert.observedDate)}</div>
                    <p>${escapeHtml(alert.suggestion || "")}</p>
                </div>
            `).join("")}
        </div>
    `;
}

function renderChartCard(series) {
    const path = buildChartPath(series.points || []);
    return `
        <div class="chart-card">
            <div class="section-head">
                <div>
                    <h4>${escapeHtml(series.metricName)}</h4>
                    <p>${escapeHtml(series.unit || "")}</p>
                </div>
            </div>
            <div class="chart-meta">
                <span>最新值 ${formatMetric(series.latestValue, series.unit)}</span>
                <span>均值 ${formatMetric(series.averageValue, series.unit)}</span>
            </div>
            <svg viewBox="0 0 320 160" preserveAspectRatio="none" aria-hidden="true">
                <polyline points="${path}" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></polyline>
            </svg>
        </div>
    `;
}

function renderPagination(data, prefix) {
    return `
        <div class="pagination">
            <span class="muted-text">第 ${data.page} / ${data.totalPages} 页，共 ${data.totalItems} 条</span>
            <div class="table-actions">
                <button class="btn-ghost" type="button" data-action="${prefix}-prev" ${data.page <= 1 ? "disabled" : ""}>上一页</button>
                <button class="btn-ghost" type="button" data-action="${prefix}-next" ${data.page >= data.totalPages ? "disabled" : ""}>下一页</button>
            </div>
        </div>
    `;
}

function renderEmpty(text) {
    return `<div class="empty-shell">${escapeHtml(text)}</div>`;
}

async function handleClick(event) {
    const button = event.target.closest("[data-action]");
    if (!button) {
        return;
    }

    const { action, route, mode, id } = button.dataset;

    try {
        switch (action) {
            case "auth-tab":
                state.authMode = mode;
                render();
                break;
            case "navigate":
                window.location.hash = route;
                break;
            case "logout":
                await api("/api/auth/logout", { method: "POST" });
                resetToAuth();
                break;
            case "refresh-dashboard":
                await loadDashboard();
                render();
                break;
            case "refresh-visualization":
                await loadVisualization();
                render();
                break;
            case "open-record-create":
                openRecordModal();
                break;
            case "open-record-edit":
                openRecordModal(findRecordById(Number(id)));
                break;
            case "delete-record":
                await deleteRecord(Number(id));
                break;
            case "records-prev":
                state.records.page -= 1;
                await loadRecords();
                render();
                break;
            case "records-next":
                state.records.page += 1;
                await loadRecords();
                render();
                break;
            case "open-alert-status":
                openAlertModal(findAlertById(Number(id)));
                break;
            case "delete-alert":
                await deleteAlert(Number(id));
                break;
            case "alerts-prev":
                state.alerts.page -= 1;
                await loadAlerts();
                render();
                break;
            case "alerts-next":
                state.alerts.page += 1;
                await loadAlerts();
                render();
                break;
            case "close-modal":
                closeModal();
                break;
            default:
                break;
        }
    } catch (error) {
        handleError(error);
    }
}

async function handleSubmit(event) {
    try {
        switch (event.target.id) {
            case "loginForm":
                event.preventDefault();
                await submitLogin(event.target);
                break;
            case "registerForm":
                event.preventDefault();
                await submitRegister(event.target);
                break;
            case "profileForm":
                event.preventDefault();
                await submitProfile(event.target);
                break;
            case "recordForm":
                event.preventDefault();
                await submitRecord(event.target);
                break;
            case "alertForm":
                event.preventDefault();
                await submitAlert(event.target);
                break;
            case "importForm":
                event.preventDefault();
                await submitImport(event.target);
                break;
            case "aiForm":
                event.preventDefault();
                await submitAi(event.target);
                break;
            default:
                break;
        }
    } catch (error) {
        handleError(error);
    }
}

async function handleChange(event) {
    try {
        if (event.target.id === "recordRiskFilter") {
            state.records.riskLevel = event.target.value;
            state.records.page = 1;
            await loadRecords();
            render();
        }
        if (event.target.id === "alertStatusFilter") {
            state.alerts.status = event.target.value;
            state.alerts.page = 1;
            await loadAlerts();
            render();
        }
        if (event.target.id === "alertSeverityFilter") {
            state.alerts.severity = event.target.value;
            state.alerts.page = 1;
            await loadAlerts();
            render();
        }
    } catch (error) {
        handleError(error);
    }
}

async function submitLogin(form) {
    const payload = formToJson(form);
    const session = await api("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(payload)
    });
    state.session = session;
    state.importResult = null;
    state.ai.result = null;
    syncRoute();
    await loadAllData();
    render();
    showToast("登录成功");
}

async function submitRegister(form) {
    const payload = formToJson(form);
    const session = await api("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(payload)
    });
    state.session = session;
    state.importResult = null;
    state.ai.result = null;
    syncRoute();
    await loadAllData();
    render();
    showToast("注册成功");
}

async function submitProfile(form) {
    await api("/api/profile", {
        method: "PUT",
        body: JSON.stringify(formToJson(form, ["age"], ["heightCm", "weightKg"]))
    });
    await Promise.all([loadDashboard(), loadProfileDetail(), loadVisualization()]);
    render();
    showToast("个人档案已保存");
}

async function submitRecord(form) {
    const formData = formToJson(
        form,
        ["systolicPressure", "diastolicPressure", "heartRate", "exerciseMinutes", "stepsCount", "waterIntakeMl", "stressLevel", "moodScore"],
        ["weightKg", "waistCircumferenceCm", "fastingBloodSugar", "postprandialBloodSugar", "bodyTemperature", "bloodOxygen", "cholesterolTotal", "sleepHours"]
    );
    const recordId = form.dataset.recordId;
    const url = recordId ? `/api/records/${recordId}` : "/api/records";
    const method = recordId ? "PUT" : "POST";
    await api(url, { method, body: JSON.stringify(formData) });
    closeModal();
    await loadAllData();
    render();
    showToast(recordId ? "健康记录已更新" : "健康记录已保存");
}

async function submitAlert(form) {
    const alertId = form.dataset.alertId;
    await api(`/api/alerts/${alertId}/status`, {
        method: "PUT",
        body: JSON.stringify(formToJson(form))
    });
    closeModal();
    await Promise.all([loadDashboard(), loadAlerts(), loadProfileDetail()]);
    render();
    showToast("预警状态已更新");
}

async function submitImport(form) {
    const payload = new FormData(form);
    const result = await api("/api/imports/health-document", {
        method: "POST",
        body: payload,
        isMultipart: true
    });
    state.importResult = result;
    await loadAllData();
    render();
    showToast("文档识别与入档已完成");
}

async function submitAi(form) {
    state.ai.loading = true;
    render();
    const payload = formToJson(form, ["focusRecordId"]);
    if (!payload.focusRecordId) {
        payload.focusRecordId = null;
    }
    state.ai.result = await api("/api/ai/analysis", {
        method: "POST",
        body: JSON.stringify(payload)
    });
    state.ai.loading = false;
    render();
    showToast("AI 分析已生成");
}

function openRecordModal(record = null) {
    openModal(`
        <div class="section-head">
            <h3>${record ? "编辑健康记录" : "新增健康记录"}</h3>
            <button class="btn-ghost" type="button" data-action="close-modal">关闭</button>
        </div>
        <form id="recordForm" class="form-grid" data-record-id="${record?.id || ""}">
            ${renderRecordFields(record)}
            <div class="full-span">
                <button class="btn" type="submit">${record ? "保存修改" : "保存记录"}</button>
            </div>
        </form>
    `);
}

function openAlertModal(alert) {
    if (!alert) {
        showToast("未找到对应预警", true);
        return;
    }
    openModal(`
        <div class="section-head">
            <h3>更新预警状态</h3>
            <button class="btn-ghost" type="button" data-action="close-modal">关闭</button>
        </div>
        <form id="alertForm" class="page-grid" data-alert-id="${alert.id}">
            <div class="muted-text">${escapeHtml(alert.title)}</div>
            <label class="form-field">
                处理状态
                <select name="status">
                    ${renderSelectOptions(enumLabels.alertStatus, alert.status)}
                </select>
            </label>
            <label class="form-field">
                处理说明
                <textarea name="handledNote">${escapeHtml(alert.handledNote || "")}</textarea>
            </label>
            <button class="btn" type="submit">保存状态</button>
        </form>
    `);
}

function renderRecordFields(record = {}) {
    return `
        <label class="form-field"><span>日期</span><input name="recordDate" type="date" value="${escapeAttr(record.recordDate || todayString())}" required></label>
        <label class="form-field"><span>体重(kg)</span><input name="weightKg" type="number" step="0.01" min="20" max="300" value="${escapeAttr(record.weightKg ?? "")}"></label>
        <label class="form-field"><span>腰围(cm)</span><input name="waistCircumferenceCm" type="number" step="0.01" min="40" max="200" value="${escapeAttr(record.waistCircumferenceCm ?? "")}"></label>
        <label class="form-field"><span>收缩压</span><input name="systolicPressure" type="number" min="40" max="250" value="${escapeAttr(record.systolicPressure ?? "")}"></label>
        <label class="form-field"><span>舒张压</span><input name="diastolicPressure" type="number" min="30" max="180" value="${escapeAttr(record.diastolicPressure ?? "")}"></label>
        <label class="form-field"><span>心率</span><input name="heartRate" type="number" min="30" max="220" value="${escapeAttr(record.heartRate ?? "")}"></label>
        <label class="form-field"><span>空腹血糖</span><input name="fastingBloodSugar" type="number" step="0.01" min="2" max="30" value="${escapeAttr(record.fastingBloodSugar ?? "")}"></label>
        <label class="form-field"><span>餐后血糖</span><input name="postprandialBloodSugar" type="number" step="0.01" min="2" max="30" value="${escapeAttr(record.postprandialBloodSugar ?? "")}"></label>
        <label class="form-field"><span>体温</span><input name="bodyTemperature" type="number" step="0.1" min="34" max="43" value="${escapeAttr(record.bodyTemperature ?? "")}"></label>
        <label class="form-field"><span>血氧(%)</span><input name="bloodOxygen" type="number" step="0.1" min="70" max="100" value="${escapeAttr(record.bloodOxygen ?? "")}"></label>
        <label class="form-field"><span>总胆固醇</span><input name="cholesterolTotal" type="number" step="0.01" min="2" max="15" value="${escapeAttr(record.cholesterolTotal ?? "")}"></label>
        <label class="form-field"><span>睡眠时长(h)</span><input name="sleepHours" type="number" step="0.1" min="0" max="24" value="${escapeAttr(record.sleepHours ?? "")}"></label>
        <label class="form-field"><span>运动时长(min)</span><input name="exerciseMinutes" type="number" min="0" max="1440" value="${escapeAttr(record.exerciseMinutes ?? "")}"></label>
        <label class="form-field"><span>步数</span><input name="stepsCount" type="number" min="0" max="100000" value="${escapeAttr(record.stepsCount ?? "")}"></label>
        <label class="form-field"><span>饮水量(ml)</span><input name="waterIntakeMl" type="number" min="0" max="10000" value="${escapeAttr(record.waterIntakeMl ?? "")}"></label>
        <label class="form-field"><span>压力等级</span><input name="stressLevel" type="number" min="1" max="10" value="${escapeAttr(record.stressLevel ?? "")}"></label>
        <label class="form-field"><span>情绪评分</span><input name="moodScore" type="number" min="1" max="10" value="${escapeAttr(record.moodScore ?? "")}"></label>
        <label class="form-field full-span"><span>症状</span><textarea name="symptoms">${escapeHtml(record.symptoms || "")}</textarea></label>
        <label class="form-field full-span"><span>用药记录</span><textarea name="medicationTaken">${escapeHtml(record.medicationTaken || "")}</textarea></label>
        <label class="form-field full-span"><span>备注</span><textarea name="notes">${escapeHtml(record.notes || "")}</textarea></label>
    `;
}

function openModal(html) {
    elements.modalPanel.innerHTML = html;
    elements.modalBackdrop.hidden = false;
    document.body.classList.add("modal-open");
}

function closeModal() {
    elements.modalPanel.innerHTML = "";
    elements.modalBackdrop.hidden = true;
    document.body.classList.remove("modal-open");
}

function findRecordById(id) {
    return state.records.items.find((item) => item.id === id)
        || state.profileDetail.recentRecords.find((item) => item.id === id)
        || state.visualization?.latestRecords?.find((item) => item.id === id)
        || null;
}

function findAlertById(id) {
    return state.alerts.items.find((item) => item.id === id)
        || state.profileDetail.recentAlerts.find((item) => item.id === id)
        || null;
}

async function deleteRecord(id) {
    if (!window.confirm("确认删除这条健康记录吗？相关预警也会一起移除。")) {
        return;
    }
    await api(`/api/records/${id}`, { method: "DELETE" });
    await loadAllData();
    render();
    showToast("健康记录已删除");
}

async function deleteAlert(id) {
    if (!window.confirm("确认删除这条预警信息吗？")) {
        return;
    }
    await api(`/api/alerts/${id}`, { method: "DELETE" });
    await Promise.all([loadDashboard(), loadAlerts(), loadProfileDetail()]);
    render();
    showToast("预警信息已删除");
}

function resetToAuth() {
    state.session = null;
    state.dashboard = null;
    state.visualization = null;
    state.importResult = null;
    state.ai = { loading: false, result: null };
    state.route = "dashboard";
    window.location.hash = "";
    render();
}

async function api(url, options = {}) {
    const config = {
        method: options.method || "GET",
        body: options.body,
        credentials: "same-origin",
        headers: options.isMultipart ? {} : { ...(options.body ? { "Content-Type": "application/json" } : {}) }
    };

    const response = await fetch(url, config);
    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const data = text ? safeJsonParse(text) : null;

    if (!response.ok) {
        if (response.status === 401 && !options.suppressUnauthorizedRedirect) {
            resetToAuth();
        }
        const details = data?.details ? Object.values(data.details).join("；") : "";
        const message = data?.message || "请求失败";
        throw new Error(details ? `${message}：${details}` : message);
    }

    return data;
}

function formToJson(form, integerFields = [], decimalFields = []) {
    const formData = new FormData(form);
    const payload = {};
    for (const [key, value] of formData.entries()) {
        if (value instanceof File) {
            payload[key] = value;
            continue;
        }
        const normalized = value.trim();
        if (integerFields.includes(key)) {
            payload[key] = normalized ? Number.parseInt(normalized, 10) : null;
        } else if (decimalFields.includes(key)) {
            payload[key] = normalized ? Number.parseFloat(normalized) : null;
        } else {
            payload[key] = normalized || null;
        }
    }
    return payload;
}

function renderSelectOptions(labels, selected) {
    return Object.entries(labels).map(([value, label]) => `
        <option value="${escapeAttr(value)}" ${selected === value ? "selected" : ""}>${escapeHtml(label)}</option>
    `).join("");
}

function renderBadge(value, type) {
    if (!value) {
        return '<span class="badge low">-</span>';
    }
    const labelMap = type === "risk" ? enumLabels.riskLevel : type === "status" ? enumLabels.alertStatus : enumLabels.alertSeverity;
    let className = "low";
    if (value === "MEDIUM" || value === "REVIEWED") {
        className = "medium";
    } else if (value === "HIGH" || value === "PENDING") {
        className = "high";
    } else if (value === "CRITICAL") {
        className = "critical";
    }
    return `<span class="badge ${className}">${escapeHtml(labelMap[value] || value)}</span>`;
}

function buildChartPath(points) {
    if (!points || points.length === 0) {
        return "";
    }
    const values = points.map((item) => Number(item.value));
    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = max - min || 1;
    const width = 320;
    const height = 160;
    const padding = 16;
    const step = points.length > 1 ? (width - padding * 2) / (points.length - 1) : 0;
    return points.map((item, index) => {
        const x = padding + step * index;
        const y = height - padding - ((Number(item.value) - min) / range) * (height - padding * 2);
        return `${x},${y}`;
    }).join(" ");
}

function formatBloodPressure(systolic, diastolic) {
    if (systolic == null && diastolic == null) {
        return "-";
    }
    return `${systolic ?? "-"} / ${diastolic ?? "-"}`;
}

function formatBloodSugar(fasting, postprandial) {
    if (fasting == null && postprandial == null) {
        return "-";
    }
    return `空腹 ${formatMetric(fasting, "mmol/L")} / 餐后 ${formatMetric(postprandial, "mmol/L")}`;
}

function formatMetric(value, unit = "") {
    if (value == null || value === "") {
        return "-";
    }
    const normalized = typeof value === "number" ? value : Number(value);
    const text = Number.isFinite(normalized)
        ? new Intl.NumberFormat("zh-CN", { maximumFractionDigits: 2 }).format(normalized)
        : String(value);
    return unit ? `${text} ${unit}` : text;
}

function formatSignedMetric(value, unit = "") {
    if (value == null || value === "") {
        return "-";
    }
    const number = Number(value);
    const prefix = number > 0 ? "+" : "";
    return `${prefix}${formatMetric(number, unit)}`;
}

function formatDate(value) {
    return value ? String(value).slice(0, 10) : "-";
}

function truncate(value, length) {
    if (!value) {
        return "-";
    }
    return value.length > length ? `${value.slice(0, length)}...` : value;
}

function todayString() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function safeJsonParse(text) {
    try {
        return JSON.parse(text);
    } catch (error) {
        return null;
    }
}

function handleError(error) {
    state.ai.loading = false;
    if (elements.app) {
        render();
    }
    showToast(error.message || "操作失败，请稍后重试", true);
}

function showToast(message, isError = false) {
    elements.toast.textContent = message;
    elements.toast.style.background = isError ? "rgba(181, 63, 56, 0.96)" : "rgba(31, 41, 51, 0.94)";
    elements.toast.classList.add("visible");
    clearTimeout(toastTimer);
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

function escapeAttr(value) {
    return escapeHtml(value);
}
