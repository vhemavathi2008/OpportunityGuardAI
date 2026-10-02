<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Create account · OpportunityGuard AI</title>
  <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body>
<div class="auth">
  <section class="auth-side">
    <h1>OpportunityGuard AI</h1>
    <p>Never miss your next opportunity.</p>
    <p style="font-size:16px">Every account is private. Your opportunities, notifications and email records are visible only to you.</p>
    <small>Chennai Institute of Technology<br>Department of Computer Science and Engineering</small>
  </section>
  <section class="auth-form">
    <div class="auth-box">
      <h2>Create account</h2>
      <p class="muted" style="margin:0 0 20px">Passwords need at least 8 characters.</p>
      <c:if test="${not empty errors}">
        <div class="alert err" role="alert"><ul><c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach></ul></div>
      </c:if>
      <form method="post" action="${ctx}/register" novalidate>
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <div class="field"><label for="fullName">Full name</label>
          <input id="fullName" type="text" name="fullName" value="<c:out value='${fullName}'/>" autocomplete="name" required autofocus></div>
        <div class="field"><label for="email">Email</label>
          <input id="email" type="email" name="email" value="<c:out value='${email}'/>" autocomplete="username" required></div>
        <div class="field"><label for="password">Password</label>
          <input id="password" type="password" name="password" autocomplete="new-password" required></div>
        <div class="field"><label for="confirmPassword">Confirm password</label>
          <input id="confirmPassword" type="password" name="confirmPassword" autocomplete="new-password" required></div>
        <button class="btn" type="submit">Create account</button>
      </form>
      <p class="auth-alt">Already registered? <a href="${ctx}/login">Log in</a></p>
    </div>
  </section>
</div>
</body>
</html>
