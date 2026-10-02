<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Profile"/><c:set var="active" value="profile"/>
<%@ include file="_header.jspf" %>

<div class="grid g2" style="align-items:start">
  <section class="card">
    <h2>Your details</h2>
    <form method="post" action="${ctx}/profile">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <div class="field"><label for="fullName">Full name</label>
        <input id="fullName" type="text" name="fullName" maxlength="100" value="<c:out value='${profileUser.fullName}'/>" required></div>
      <div class="field"><label for="email">Email</label>
        <input id="email" type="email" value="<c:out value='${profileUser.email}'/>" disabled></div>
      <p class="muted">Member since <fmt:formatDate value="${profileUser.createdAt}" pattern="dd MMM yyyy"/></p>
      <button class="btn" type="submit">Save profile</button>
    </form>
  </section>
  <section class="card">
    <h2>Your activity</h2>
    <dl class="kv">
      <dt>Opportunities</dt><dd>${stats.total}</dd>
      <dt>Registered</dt><dd>${stats.registered}</dd>
      <dt>Final submissions</dt><dd>${stats.finalSubmitted}</dd>
      <dt>Unread notifications</dt><dd>${stats.unreadNotifications}</dd>
      <dt>Emails stored</dt><dd>${emailCount}</dd>
    </dl>
  </section>
</div>
<%@ include file="_footer.jspf" %>
