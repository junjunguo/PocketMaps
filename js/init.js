(function($){
  $(function(){

    $('.button-collapse').sideNav();
    $('.parallax').parallax();

  }); // end of document ready
})(jQuery); // end of jQuery name space



// load js/wow.min.js
var wow = new WOW(
    {
      boxClass:        'wow',      // animated element css class (default is wow)
      animateClass:    'animated', // animation css class (default is animated)
      offset:          0,          // distance to the element when triggering the animation (default is 0)
      mobile:          true,       // trigger animations on mobile devices (default is true)
      live:            true,       // act on asynchronously loaded content (default is true)
      callback:        function (box) {
        // the callback is fired every time an animation is started
        // the argument that is passed in is the DOM node being animated
      },
      scrollContainer: null // optional scroll container selector, otherwise use window
    }
);

wow.init();