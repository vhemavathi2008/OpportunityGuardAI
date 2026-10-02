<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Gmail Sync"/><c:set var="active" value="gmail"/>
<%@ include file="_header.jspf" %>

<div class="stack">
  <section class="card">
    <div class="card-head"><h2>Connection</h2>
      <span class="badge ${connected ? 't-good' : 't-unknown'}">${connected ? 'Connected' : 'Not connected'}</span></div>
    <c:if test="${not oauthConfigured}">
      <div class="alert err">Google OAuth is not configured on this server, so Gmail cannot be connected. Set <code>GOOGLE_CLIENT_ID</code> and <code>GOOGLE_CLIENT_SECRET</code> (see README, section 6).</div>
    </c:if>
    <c:if test="${not geminiConfigured}">
      <div class="alert info">Gemini is not configured (<code>GEMINI_API_KEY</code>). Emails will be stored but not analysed, and no opportunities will be created from them.</div>
    </c:if>
    <p class="muted">Access is read-only and granted through Google's own consent screen. Your Google password never reaches this app.
      <c:if test="${not empty lastSync}"> Last sync: <fmt:formatDate value="${lastSync}" pattern="dd MMM yyyy, HH:mm"/>.</c:if></p>
    <div class="actions">
      <c:choose>
        <c:when test="${connected}">
          <form class="inline" method="post" action="${ctx}/gmail/sync">
            <input type="hidden" name="_csrf" value="${csrfToken}"><button class="btn" type="submit">Sync now</button></form>
          <form class="inline" method="post" action="${ctx}/gmail/disconnect" data-confirm="Disconnect Gmail? Stored emails and opportunities are kept.">
            <input type="hidden" name="_csrf" value="${csrfToken}"><button class="btn danger" type="submit">Disconnect</button></form>
        </c:when>
        <c:otherwise>
          <form class="inline" method="post" action="${ctx}/gmail/connect">
            <input type="hidden" name="_csrf" value="${csrfToken}"><button class="btn" type="submit" ${oauthConfigured ? '' : 'disabled'}>Connect Gmail</button></form>
        </c:otherwise>
      </c:choose>
    </div>
  </section>

  <section class="grid g4" style="grid-template-columns:repeat(2,1fr)">
    <div class="card stat"><div class="n">${totalEmails}</div><div class="l">Emails stored for you</div></div>
    <div class="card stat"><div class="n">${analyzedEmails}</div><div class="l">Analysed by Gemini</div></div>
  </section>

  <section class="card">
    <h2>Your email records</h2>
    <c:choose>
      <c:when test="${empty records}"><div class="empty">No emails stored yet. Connect Gmail and press Sync now.</div></c:when>
      <c:otherwise>
        <div class="table-wrap"><table>
          <thead><tr><th>Sender</th><th>Subject</th><th>Stored</th><th>Analysis</th></tr></thead>
          <tbody>
            <c:forEach var="r" items="${records}">
              <tr>
                <td><span class="clamp"><c:out value="${r.sender}"/></span></td>
                <td><span class="clamp"><c:out value="${r.subject}"/></span></td>
                <td><fmt:formatDate value="${r.createdAt}" pattern="dd MMM yyyy, HH:mm"/></td>
                <td><span class="badge ${r.analyzed ? 't-good' : 't-unknown'}">${r.analyzed ? 'Analysed' : 'Pending'}</span></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </c:otherwise>
    </c:choose>
  </section>
</div>
<%@ include file="_footer.jspf" %>
