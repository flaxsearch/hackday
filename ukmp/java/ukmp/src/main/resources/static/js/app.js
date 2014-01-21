var ukmpApp = angular.module('ukmpApp', [ 'ngRoute', 'ukmpControllers' ]);

ukmpApp.config([ '$routeProvider', 
	function($routeProvider) {
		$routeProvider.when('/search', {
			templateUrl : 'template/search.html',
			controller : 'UKMP_SearchCtrl'
		}).when('/about', {
			templateUrl : 'template/about.html',
			controller : 'UKMP_AboutCtrl'
		}).otherwise({
			redirectTo : '/about'
		});
	} 
]);
