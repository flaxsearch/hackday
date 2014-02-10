var ukmpControllers = angular.module('ukmpControllers', [ 'ui.bootstrap', 'ngSanitize' ]);

ukmpControllers.controller('UKMP_PageCtrl', [ '$scope', '$location', function($scope, $location) {
	
	$scope.searchForm = {};
	
	// Event handler to catch search from nav bar - only want this to do
	// something when *not* on search page
	$scope.$on('Search', function(evt, query) {
		if (!$location.path().match(/^\/search.*/)) {
			// Redirect to the search page
			$location.path('/search');
		}
	});
	
}]);

/*
 * Search Controller. Handles all functions on the search page, plus
 * access through the search form in the navbar.
 */
ukmpControllers.controller('UKMP_SearchCtrl', [ '$scope', '$http', '$location', '$routeParams', 
                                                function($scope, $http, $location, $routeParams) {
	
	var self = this;
	
	$scope.setPage = function(page) {
		var params = {
			p: page - 1
		};
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			params.sortby = $scope.searchState.sortField;
			params.sortasc = $scope.searchState.sortAscending;
			self.copyFilters(params);
		}

		self.updateModel(params);
	}
	
	// Event handler to catch search from nav bar when already on search page
	$scope.$on('Search', function(evt, query) {
		var params = { q: query };
		if ($scope.searchState) {
			// Pass the sortby values into the query
			params.sortby = $scope.searchState.sortField;
			params.sortasc = $scope.searchState.sortAscending;
		}
		self.updateModel(params);
	});
	
	// Event handler to catch browse link - reset search
	$scope.$on('Browse', function() {
		$scope.searchState = {};
		$scope.setPage(1);
		// Reset the search form
		if ($scope.query) {
			// Have to reset the query in the parent scope...
			$scope.$parent.query = "";
			$scope.searchForm.$setPristine();
		}
	});
	
	$scope.changeSortOrder = function(sortdetails) {
		var details = sortdetails.split(/\s+/);
		var sortAscending = (details[1] == 'asc');
		
		var params = {
			sortby: details[0],
			sortasc: sortAscending
		}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
		}
		
		self.updateModel(params);
	}
	
	$scope.filter = function(facet) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			params.sortby = $scope.searchState.sortField;
			params.sortasc = $scope.searchState.sortAscending;
			self.copyFilters(params);
			if (facet.value.charAt(0) == '[') {
				// This is a facetQuery - don't quote the terms
				params.fq.push(facet.field + ":" + facet.value);
			} else {
				// This is a straight facet - quote the value
				params.fq.push(facet.field + ':"' + facet.value + '"')
			}
		}
		
		self.updateModel(params);
	}
	
	$scope.remove_filter = function(field, value) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			params.sortby = $scope.searchState.sortField;
			params.sortasc = $scope.searchState.sortAscending;
			self.copyFilters(params, field + ':"' + value + '"');
		}
		
		self.updateModel(params);
	}
	
	$scope.change_highlighting = function() {
		var params = {}
		// Only re-run search if we have a query defined - otherwise there will
		// be no highlighting in the tweets, so no need to refresh
		if ($scope.searchState && $scope.searchState.query !== '*') {
			params.q = $scope.searchState.query;
			params.p = $scope.searchState.pageNumber;
			params.sortby = $scope.searchState.sortField;
			params.sortasc = $scope.searchState.sortAscending;
			self.copyFilters(params);
			
			self.updateModel(params);
		}
	}
	
	this.copyFilters = function(params, skip) {
		params.fq = [];
		if ($scope.searchState.appliedFilters) {
			var fields = Object.keys($scope.searchState.appliedFilters)
			if (fields.length > 0) {
				for (var i = 0; i < fields.length; i ++) {
					var field = fields[i];
					var filters = $scope.searchState.appliedFilters[field].facets;
					for (var j = 0; j < filters.length; j ++) {
						var fq = field + ':"' + filters[j].value + '"';
						if (fq !== skip) {
							params.fq.push(fq);
						}
					}
				}
			}
		}
	}
	
	this.updateModel = function(params) {
		// Copy common params
		if ($scope.searchState) {
			params.hl = $scope.searchState.highlightingEnabled;
		}
		
		$http({
			method: 'GET',
			url: '/service/browse',
			params: params
		}).success(function(data) {
			if (data.errorMessage) {
				if (window.console) {
					console.log(data.errorMessage);
				}
				
				// Error state - show an error message, hide the search results
				$scope.error = 'A server error has occurred - try again later.';
				$('#error-message').show();
				$('#search-status').hide();
				$('#search-results').hide();
			} else {
				// Everything is okay - hide the error block, display the search results
				$('#error-message').hide();
				$('#search-status').show();
				$('#search-results').show();
				
				// Update the basic scope data
				$scope.tweets = data.tweets;
				self.addTweetTextLinks($scope.tweets);
				$scope.searchState = data.searchState;
				// Incoming page number is 0-indexed
				$scope.currentPage = $scope.searchState.pageNumber + 1;
				$scope.numPages = data.numResults / data.pageSize;
				$scope.totalItems = data.numResults;
				$scope.sortDetails = $scope.searchState.sortField  
					+ ($scope.searchState.sortAscending ? ' asc' : ' desc');
				
				// Booleans indicating whether or not to display the searched/filtered by displays
				$scope.searched = $scope.searchState.query !== "*";
				$scope.filtered = Object.keys($scope.searchState.appliedFilters).length > 0;
			}
		});
	}
	
	this.addTweetTextLinks = function(tweets) {
		// Regex for matching a twitter username
		var usernameRx = /(@(\w+))/g;
		// Regex for matching a general link
		var linkRx = /(http:\/\/\S+)\b/g;
		// Regex for matching a hash tag
		var hashtagRx = /(#(\S+))/g;
		// Loop through the text, adding links around each username found
		for (var i = 0; i < tweets.length; i ++) {
			var text = tweets[i].text;
			// Replace the links first, *then* the usernames
			var replaceText = text.replace(linkRx, '<a href="$1" target="_tweets">$1</a>')
			replaceText = replaceText.replace(usernameRx, '<a href="http://twitter.com/$2" target="_tweets">$1</a>');
			// Link hashtags - note %23 subbing for hash character
			replaceText = replaceText.replace(hashtagRx, '<a href="http://twitter.com/search?q=%23$2&src=hash" target="_tweets">$1</a>');
			tweets[i].text = replaceText;
		}
	}
	
	this.init = function() {
		// Check for a value in the search form - if present, search on it
		// (we are coming in from another page via the search event)
		if ($scope.query) {
			self.updateModel({ q: $scope.query });
		} else if ($routeParams.query) {
			// Incoming link with query included (most likely from word cloud)
			// - set query in form, redirect to /search.
			$scope.$parent.query = $routeParams.query;
			$location.path('/search');
		} else {
			// User has clicked the browse link - just grab first page of tweets
			$scope.setPage(1);
		}
	}
	
	self.init();
	
}])
.directive('facetList', function() {
	return {
		restrict: 'E',
		scope: {
			facets: '=facets',
			click: '&facetClick'
		},
		templateUrl: 'template/directive/facet_list.html'
	};
})
.directive('facetAccordion', function() {
	return {
		restrict: 'E',
		scope: {
			facets: '=',
			isOpen: '=',
			click: '&'
		},
		templateUrl: 'template/directive/facet_accordion_group.html'
	}
})
.directive('facetQueryAccordion', function() {
	return {
		restrict: 'E',
		scope: {
			facets: '=',
			isOpen: '=',
			click: '&'
		},
		templateUrl: 'template/directive/facet_query_accordion_group.html'
	}
});


/*
 * About controller. Controls the About page. 
 */
ukmpControllers.controller('UKMP_AboutCtrl', [ '$scope', '$http', '$location', function($scope, $http) {

	var self = this;

	this.initialiseCloud = function() {
		var fill = d3.scale.category20();

		d3.layout.cloud()
			.size([ 750, 300 ])
			.words($scope.terms.map(function(term, idx, arr) {
				return {
					text : term.term,
					size : 10 + (((arr.length - idx) / arr.length) * 60)
				};
			}))
			.padding(1)
			.rotate(function() {
				return ~~(Math.random() * 2) * 90;
			})
			.font("Impact")
			.fontSize(function(d) {
				return d.size;
			})
			.spiral("rectangular")
			.on("end", draw)
			.start();

		function draw(words) {
			d3.select("#word_cloud").append("svg")
				.attr("width", 750)
				.attr("height", 300)
				.append("g")
				.attr("transform", "translate(370,150)")
				.selectAll("text")
				.data(words)
				.enter()
				.append("a")
				.attr("xlink:href", function(d) {
					return "/#/search/" + d.text;
				})
				.style("text-decoration", "none")
				.append("text")
				.style("font-size", function(d) {
					return d.size + "px";
				})
				.style("font-family", "Impact")
				.style("fill", function(d, i) {
					var colours = [ 'Black', 'Red', 'Blue', 'Orange', 'Green', 'Purple' ];
					return colours[~~(Math.random() * colours.length)];
				})
				.attr("text-anchor", "middle")
				.attr("transform", function(d) {
					return "translate(" + [ d.x, d.y ] + ")rotate(" + d.rotate + ")";
				})
				.text(function(d) {
					return d.text;
				});
		}

	}

	this.init = function() {
		// Get the terms data
		$http({
			'method' : 'GET',
			'url' : '/service/terms'
		}).success(function(data) {
			$scope.terms = data.terms;
			self.initialiseCloud();
		});
		
		// Reset the search form
		if ($scope.query) {
			// Have to reset the query in the parent scope...
			$scope.$parent.query = "";
			$scope.searchForm.$setPristine();
		}
	}
	
	self.init();

} ]);