<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="og" tagdir="/WEB-INF/tags" %>

<c:set var="pageTitle" value="Opportunity details"/>
<c:set var="active" value="opportunities"/>

<%@ include file="_header.jspf" %>

<p>
  <a href="${ctx}/opportunities">&larr; Back to opportunities</a>
</p>

<div class="grid g2" style="grid-template-columns:1.4fr 1fr;align-items:start">

  <section class="card">

    <div class="card-head" style="align-items:flex-start">
      <div>

        <h2 style="font-size:20px;margin-bottom:6px">
          <c:out value="${opportunity.title}"/>
        </h2>

        <div class="pill-row">
          <span class="chip">
            <c:out value="${opportunity.type}"/>
          </span>

          <span class="badge ${opportunity.statusClass}">
            <c:out value="${opportunity.status}"/>
          </span>

          <span class="badge ${opportunity.trustClass}">
            <c:out value="${opportunity.trustVerdict}"/>
          </span>
        </div>

      </div>
    </div>

    <dl class="kv">

      <dt>Organization</dt>
      <dd>
        <c:out value="${empty opportunity.organization ? '—' : opportunity.organization}"/>
      </dd>

      <dt>Deadline</dt>
      <dd>
        <og:deadline o="${opportunity}"/>
      </dd>

      <dt>Eligibility</dt>
      <dd>
        <c:out value="${empty opportunity.eligibility ? '—' : opportunity.eligibility}"/>
      </dd>

      <dt>Description</dt>
      <dd>
        <c:out value="${empty opportunity.description ? '—' : opportunity.description}"/>
      </dd>

      <dt>Trust verdict</dt>
      <dd>
        <c:out value="${opportunity.trustVerdict}"/>

        <c:if test="${not empty opportunity.trustConfidence}">
          &middot; ${opportunity.trustConfidence}% model confidence
          (an AI estimate, not a guarantee)
        </c:if>
      </dd>

      <dt>Registration link</dt>
      <dd>
        <c:choose>

          <c:when test="${not empty opportunity.registrationLink}">
            <a href="<c:out value='${opportunity.registrationLink}'/>"
               target="_blank"
               rel="noopener noreferrer">
              <c:out value="${opportunity.registrationLink}"/>
            </a>
          </c:when>

          <c:otherwise>
            —
          </c:otherwise>

        </c:choose>
      </dd>

      <dt>Added</dt>
      <dd>
        <fmt:formatDate
                value="${opportunity.createdAt}"
                pattern="dd MMM yyyy, HH:mm"/>
      </dd>

    </dl>

  </section>


  <div class="stack">

    <!-- PARTICIPATION STATUS -->
    <section class="card">

      <h2>Participation status</h2>

      <p class="muted" style="margin-top:-6px">
        You can update these stages manually. Idea, PPT, Demo and Final
        submission can also be marked automatically when a matching
        confirmation email is detected in Gmail.
      </p>

      <form method="post" action="${ctx}/opportunity/status">

        <input type="hidden"
               name="_csrf"
               value="${csrfToken}">

        <input type="hidden"
               name="id"
               value="${opportunity.id}">

        <div class="stages">

          <!-- REGISTERED - MANUAL ONLY -->
          <label>
            <input type="checkbox"
                   name="registered"
            ${opportunity.registered ? 'checked' : ''}>

            Registered

            <span class="badge ${opportunity.registered ? 't-good' : 't-unknown'}">
              ${opportunity.registered ? '✓ Completed' : '✗ Not completed'}
            </span>
          </label>


          <!-- IDEA SUBMITTED -->
          <label>
            <input type="checkbox"
                   name="ideaSubmitted"
            ${opportunity.ideaSubmitted ? 'checked' : ''}>

            Idea submitted

            <span class="badge ${opportunity.ideaSubmitted ? 't-good' : 't-bad'}">
              ${opportunity.ideaSubmitted ? '✓ Completed' : '✗ Not detected'}
            </span>
          </label>


          <!-- PPT SUBMITTED -->
          <label>
            <input type="checkbox"
                   name="pptSubmitted"
            ${opportunity.pptSubmitted ? 'checked' : ''}>

            PPT submitted

            <span class="badge ${opportunity.pptSubmitted ? 't-good' : 't-bad'}">
              ${opportunity.pptSubmitted ? '✓ Completed' : '✗ Not detected'}
            </span>
          </label>


          <!-- DEMO SUBMITTED -->
          <label>
            <input type="checkbox"
                   name="demoSubmitted"
            ${opportunity.demoSubmitted ? 'checked' : ''}>

            Demo submitted

            <span class="badge ${opportunity.demoSubmitted ? 't-good' : 't-bad'}">
              ${opportunity.demoSubmitted ? '✓ Completed' : '✗ Not detected'}
            </span>
          </label>


          <!-- FINAL SUBMISSION -->
          <label>
            <input type="checkbox"
                   name="finalSubmitted"
            ${opportunity.finalSubmitted ? 'checked' : ''}>

            Final submission

            <span class="badge ${opportunity.finalSubmitted ? 't-good' : 't-bad'}">
              ${opportunity.finalSubmitted ? '✓ Completed' : '✗ Not detected'}
            </span>
          </label>

        </div>

        <button class="btn" type="submit">
          Save status
        </button>

      </form>

    </section>


    <!-- REMOVE OPPORTUNITY -->
    <section class="card">

      <h2>Remove</h2>

      <form method="post"
            action="${ctx}/opportunity/delete"
            data-confirm="Delete this opportunity and its notifications?">

        <input type="hidden"
               name="_csrf"
               value="${csrfToken}">

        <input type="hidden"
               name="id"
               value="${opportunity.id}">

        <button class="btn danger" type="submit">
          Delete opportunity
        </button>

      </form>

    </section>

  </div>

</div>

<%@ include file="_footer.jspf" %>