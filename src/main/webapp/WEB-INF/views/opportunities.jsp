<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="og" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Opportunities"/><c:set var="active" value="opportunities"/>
<%@ include file="_header.jspf" %>

<div class="stack">
  <details class="card" ${showForm ? 'open' : ''}>
    <summary>+ Add an opportunity manually</summary>
    <c:if test="${not empty formErrors}">
      <div class="alert err" role="alert"><ul><c:forEach var="e" items="${formErrors}"><li><c:out value="${e}"/></li></c:forEach></ul></div>
    </c:if>
    <form method="post" action="${ctx}/opportunities/add" novalidate>
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <div class="form-grid">
        <div class="field"><label for="title">Opportunity name *</label>
          <input id="title" type="text" name="title" maxlength="255" value="<c:out value='${form.title}'/>" required></div>
        <div class="field"><label for="organization">Organization</label>
          <input id="organization" type="text" name="organization" maxlength="255" value="<c:out value='${form.organization}'/>"></div>
        <div class="field"><label for="type">Type</label>
          <select id="type" name="type">
            <c:forEach var="t" items="${types}"><option value="${t}" ${form.type == t ? 'selected' : ''}>${t}</option></c:forEach>
          </select></div>
        <div class="field"><label for="deadline">Deadline</label>
          <input id="deadline" type="date" name="deadline" value="<c:out value='${form.deadline}'/>"></div>
        <div class="field full"><label for="registrationLink">Registration link</label>
          <input id="registrationLink" type="url" name="registrationLink" maxlength="1000" placeholder="https://" value="<c:out value='${form.registrationLink}'/>"></div>
        <div class="field full"><label for="eligibility">Eligibility</label>
          <textarea id="eligibility" name="eligibility" maxlength="2000"><c:out value="${form.eligibility}"/></textarea></div>
        <div class="field full"><label for="description">Description</label>
          <textarea id="description" name="description" maxlength="2000"><c:out value="${form.description}"/></textarea></div>
      </div>
      <p class="muted" style="margin-top:0">Manually added opportunities start as "Unverified"; the app does not claim to have checked them.</p>
      <button class="btn" type="submit">Save opportunity</button>
    </form>
  </details>

  <section class="card">
    <div class="card-head">
      <h2>Your opportunities</h2>
      <form class="search" method="get" action="${ctx}/opportunities">
        <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Search name, organization, type" aria-label="Search">
        <button class="btn ghost sm" type="submit">Search</button>
      </form>
    </div>
    <c:choose>
      <c:when test="${empty opportunities}">
        <div class="empty"><c:choose>
          <c:when test="${not empty q}">No opportunities match your search.</c:when>
          <c:otherwise>You have no opportunities yet. Add one above or <a href="${ctx}/gmail">sync Gmail</a>.</c:otherwise>
        </c:choose></div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap"><table>
          <thead><tr><th>Opportunity</th><th>Type</th><th>Deadline</th><th>Eligibility</th><th>Trust</th><th>Link</th><th>Status</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="o" items="${opportunities}">
              <tr>
                <td><strong><c:out value="${o.title}"/></strong><small><c:out value="${o.organization}"/></small></td>
                <td><span class="chip"><c:out value="${o.type}"/></span></td>
                <td><og:deadline o="${o}"/></td>
                <td><span class="clamp"><c:out value="${empty o.eligibility ? '—' : o.eligibility}"/></span></td>
                <td><span class="badge ${o.trustClass}"><c:out value="${o.trustVerdict}"/></span>
                  <c:if test="${not empty o.trustConfidence}"><small>${o.trustConfidence}% model confidence</small></c:if></td>
                <td><c:choose>
                  <c:when test="${not empty o.registrationLink}"><a href="<c:out value='${o.registrationLink}'/>" target="_blank" rel="noopener noreferrer">Open</a></c:when>
                  <c:otherwise><span class="muted">—</span></c:otherwise></c:choose></td>
                <td><span class="badge ${o.statusClass}"><c:out value="${o.status}"/></span></td>
                <td><a class="btn ghost sm" href="${ctx}/opportunity?id=${o.id}">Manage</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </c:otherwise>
    </c:choose>
  </section>
</div>
<%@ include file="_footer.jspf" %>
