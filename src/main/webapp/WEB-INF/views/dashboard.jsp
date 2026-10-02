<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="og" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Dashboard"/><c:set var="active" value="dashboard"/>
<%@ include file="_header.jspf" %>

<div class="stack">
  <section class="card welcome">
    <h2>Welcome back, <c:out value="${sessionScope.userName}"/></h2>
    <p>You are tracking ${stats.total} opportunit${stats.total == 1 ? 'y' : 'ies'}; ${stats.upcoming} still open with a deadline ahead.</p>
  </section>

  <c:if test="${not gmailConnected}">
    <div class="alert info">Gmail is not connected yet. <a href="${ctx}/gmail">Connect Gmail</a> to pull opportunities from your inbox, or <a href="${ctx}/opportunities">add one manually</a>.</div>
  </c:if>

  <section class="grid g4" aria-label="Your statistics">
    <div class="card stat accent"><div class="n">${stats.total}</div><div class="l">Total opportunities</div></div>
    <div class="card stat"><div class="n">${stats.upcoming}</div><div class="l">Upcoming deadlines</div></div>
    <div class="card stat"><div class="n">${stats.registered}</div><div class="l">Registered</div></div>
    <div class="card stat"><div class="n">${stats.ideaSubmitted}</div><div class="l">Idea submitted</div></div>
    <div class="card stat"><div class="n">${stats.pptSubmitted}</div><div class="l">PPT submitted</div></div>
    <div class="card stat"><div class="n">${stats.demoSubmitted}</div><div class="l">Demo submitted</div></div>
    <div class="card stat"><div class="n">${stats.finalSubmitted}</div><div class="l">Final submission</div></div>
    <div class="card stat"><div class="n">${stats.unreadNotifications}</div><div class="l">Unread notifications</div></div>
  </section>

  <div class="grid g2">
    <section class="card">
      <div class="card-head"><h2>Upcoming deadlines (next 14 days)</h2></div>
      <c:choose>
        <c:when test="${empty upcoming}"><div class="empty">No open deadlines in the next 14 days.</div></c:when>
        <c:otherwise>
          <ul class="list">
            <c:forEach var="o" items="${upcoming}">
              <li><a href="${ctx}/opportunity?id=${o.id}"><strong><c:out value="${o.title}"/></strong></a>
                <small class="muted"><c:out value="${o.organization}"/></small>
                <div><og:deadline o="${o}"/></div></li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>

    <section class="card">
      <div class="card-head"><h2>Recent notifications</h2><a href="${ctx}/notifications">View all</a></div>
      <c:choose>
        <c:when test="${empty recentNotifications}"><div class="empty">No notifications yet. They appear when one of your deadlines gets close.</div></c:when>
        <c:otherwise>
          <ul class="list">
            <c:forEach var="n" items="${recentNotifications}">
              <li><strong><c:out value="${n.title}"/></strong>
                <c:if test="${not n.read}"> <span class="badge b-reg">New</span></c:if>
                <div class="muted"><c:out value="${n.message}"/></div></li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>
  </div>

  <section class="card">
    <div class="card-head"><h2>Recent opportunities</h2><a href="${ctx}/opportunities">View all</a></div>
    <c:choose>
      <c:when test="${empty recent}"><div class="empty">Nothing here yet. Add an opportunity or sync your Gmail.</div></c:when>
      <c:otherwise>
        <div class="table-wrap"><table>
          <thead><tr><th>Opportunity</th><th>Type</th><th>Deadline</th><th>Trust</th><th>Status</th></tr></thead>
          <tbody>
            <c:forEach var="o" items="${recent}">
              <tr>
                <td><a href="${ctx}/opportunity?id=${o.id}"><strong><c:out value="${o.title}"/></strong></a><small><c:out value="${o.organization}"/></small></td>
                <td><span class="chip"><c:out value="${o.type}"/></span></td>
                <td><og:deadline o="${o}"/></td>
                <td><span class="badge ${o.trustClass}"><c:out value="${o.trustVerdict}"/></span></td>
                <td><span class="badge ${o.statusClass}"><c:out value="${o.status}"/></span></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </c:otherwise>
    </c:choose>
  </section>
</div>
<%@ include file="_footer.jspf" %>
