<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Notifications"/><c:set var="active" value="notifications"/>
<%@ include file="_header.jspf" %>

<section class="card">
  <div class="card-head">
    <h2>Your notifications</h2>
    <c:if test="${not empty notifications}">
      <form class="inline" method="post" action="${ctx}/notifications/read">
        <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="id" value="all">
        <button class="btn ghost sm" type="submit">Mark all as read</button>
      </form>
    </c:if>
  </div>
  <c:choose>
    <c:when test="${empty notifications}"><div class="empty">No notifications yet. You get one when a deadline you are tracking is within 3 days, or has just passed without a final submission.</div></c:when>
    <c:otherwise>
      <ul class="list">
        <c:forEach var="n" items="${notifications}">
          <li class="notif ${n.read ? '' : 'unread'}">
            <div>
              <strong><c:out value="${n.title}"/></strong>
              <div><c:out value="${n.message}"/></div>
              <small class="muted"><fmt:formatDate value="${n.createdAt}" pattern="dd MMM yyyy, HH:mm"/>
                <c:if test="${not empty n.opportunityId}"> &middot; <a href="${ctx}/opportunity?id=${n.opportunityId}">View opportunity</a></c:if></small>
            </div>
            <c:if test="${not n.read}">
              <form class="inline" method="post" action="${ctx}/notifications/read">
                <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="id" value="${n.id}">
                <button class="btn ghost sm" type="submit">Mark read</button>
              </form>
            </c:if>
          </li>
        </c:forEach>
      </ul>
    </c:otherwise>
  </c:choose>
</section>
<%@ include file="_footer.jspf" %>
