<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Something went wrong · OpportunityGuard AI</title>
  <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body>
<div class="auth-form" style="min-height:100vh;background:var(--bg)">
  <div class="card auth-box" style="text-align:center">
    <h2>
      <c:choose>
        <c:when test="${pageContext.errorData.statusCode == 404}">Page not found</c:when>
        <c:when test="${pageContext.errorData.statusCode == 403}">Request blocked</c:when>
        <c:otherwise>Something went wrong</c:otherwise>
      </c:choose>
    </h2>
    <p class="muted">
      <c:choose>
        <c:when test="${pageContext.errorData.statusCode == 404}">This page does not exist, or it is not part of your account.</c:when>
        <c:when test="${pageContext.errorData.statusCode == 403}">Your security token expired. Go back, reload the page and try again.</c:when>
        <c:otherwise>The server could not complete your request. If this keeps happening, check that MySQL is running and the DB_* settings are correct.</c:otherwise>
      </c:choose>
    </p>
    <a class="btn" href="${ctx}/dashboard">Back to dashboard</a>
  </div>
</div>
</body>
</html>
