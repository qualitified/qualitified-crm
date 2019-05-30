! function(e, r) {
  "object" == typeof exports && "object" == typeof module ? module.exports = r(require("moment"), require("fullcalendar")) : "function" == typeof define && define.amd ? define(["moment", "fullcalendar"], r) : "object" == typeof exports ? r(require("moment"), require("fullcalendar")) : r(e.moment, e.FullCalendar)
}("undefined" != typeof self ? self : this, function(e, r) {
  return function(e) {
    function r(t) {
      if (n[t]) return n[t].exports;
      var a = n[t] = {
        i: t,
        l: !1,
        exports: {}
      };
      return e[t].call(a.exports, a, a.exports, r), a.l = !0, a.exports
    }
    var n = {};
    return r.m = e, r.c = n, r.d = function(e, n, t) {
      r.o(e, n) || Object.defineProperty(e, n, {
        configurable: !1,
        enumerable: !0,
        get: t
      })
    }, r.n = function(e) {
      var n = e && e.__esModule ? function() {
        return e.default
      } : function() {
        return e
      };
      return r.d(n, "a", n), n
    }, r.o = function(e, r) {
      return Object.prototype.hasOwnProperty.call(e, r)
    }, r.p = "", r(r.s = 135)
  }({
    0: function(r, n) {
      r.exports = e
    },
    1: function(e, n) {
      e.exports = r
    },
    135: function(e, r, n) {
      Object.defineProperty(r, "__esModule", {
        value: !0
      }), n(136);
      var t = n(1);
      t.datepickerLocale("fr", "fr", {
        closeText: "Fermer",
        prevText: "Précédent",
        nextText: "Suivant",
        currentText: "Aujourd'hui",
        monthNames: ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"],
        monthNamesShort: ["Janv.", "Févr.", "Mars", "Avr.", "Mai", "Juin", "Juil.", "Août", "Sept.", "Oct.", "Nov.", "Déc."],
        dayNames: ["Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"],
        dayNamesShort: ["Dim.", "Lun.", "Mar.", "Mer.", "Jeu.", "Ven.", "Sam."],
        dayNamesMin: ["D", "L", "M", "M", "J", "V", "S"],
        weekHeader: "Sem.",
        dateFormat: "dd/mm/yy",
        firstDay: 1,
        isRTL: !1,
        showMonthAfterYear: !1,
        yearSuffix: ""
      }), t.locale("fr", {
        buttonText: {
          year: "Année",
          month: "Mois",
          week: "Semaine",
          day: "Jour",
          list: "Mon planning"
        },
        allDayHtml: "Toute la<br/>journée",
        eventLimitText: "en plus",
        noEventsMessage: "Aucun événement à afficher"
      })
    },
    136: function(e, r, n) {
      ! function(e, r) {
        r(n(0))
      }(0, function(e) {
        return e.defineLocale("fr", {
          months: "Janvier_Février_Mars_Avril_Mai_Juin_Juillet_Août_Septembre_Octobre_Novembre_Décembre".split("_"),
          monthsShort: "Janv._Févr._Mars_Avr._Mai_Juin_Juil._Août_Sept._Oct._Nov._Déc.".split("_"),
          monthsParseExact: !0,
          weekdays: "Dimanche_Lundi_Mardi_Mercredi_Jeudi_Vendredi_Samedi".split("_"),
          weekdaysShort: "Dim._Lun._Mar._Mer._Jeu._Ven._Sam.".split("_"),
          weekdaysMin: "di_lu_ma_me_je_ve_sa".split("_"),
          weekdaysParseExact: !0,
          longDateFormat: {
            LT: "HH:mm",
            LTS: "HH:mm:ss",
            L: "DD/MM/YYYY",
            LL: "D MMMM YYYY",
            LLL: "D MMMM YYYY HH:mm",
            LLLL: "dddd D MMMM YYYY HH:mm"
          },
          calendar: {
            sameDay: "[Aujourd’hui à] LT",
            nextDay: "[Demain à] LT",
            nextWeek: "dddd [à] LT",
            lastDay: "[Hier à] LT",
            lastWeek: "dddd [dernier à] LT",
            sameElse: "L"
          },
          relativeTime: {
            future: "dans %s",
            past: "il y a %s",
            s: "quelques secondes",
            ss: "%d secondes",
            m: "une minute",
            mm: "%d minutes",
            h: "une heure",
            hh: "%d heures",
            d: "un jour",
            dd: "%d jours",
            M: "un mois",
            MM: "%d mois",
            y: "un an",
            yy: "%d ans"
          },
          dayOfMonthOrdinalParse: /\d{1,2}(er|)/,
          ordinal: function(e, r) {
            switch (r) {
              case "D":
                return e + (1 === e ? "er" : "");
              default:
              case "M":
              case "Q":
              case "DDD":
              case "d":
                return e + (1 === e ? "er" : "e");
              case "w":
              case "W":
                return e + (1 === e ? "re" : "e")
            }
          },
          week: {
            dow: 1,
            doy: 4
          }
        })
      })
    }
  })
});
