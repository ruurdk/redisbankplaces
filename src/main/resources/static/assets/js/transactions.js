var transactionsOverview = new Vue({
  el: '#transactionstable',
  data: {
    items: [],
    received_messages: [],
    connected: false,
    account: 'Alex',//TODO this one is hardcoded vs using the login info
    balance: '',
    question: '',
    searchitems: [],
  },
  mounted() {
    this.getInitialData()
  },
  watch: {
    // whenever question changes, this function will run
    question: function (newQuestion, oldQuestion) {
      this.searchitems = []
      this.debouncedGetAnswer()
    }
  },
  created: function () {
    this.debouncedGetAnswer = _.debounce(this.getAnswer, 100)
  },
  methods: {
    getInitialData: function () {
      var transactionsUrl = '/api/places'
      var vm = this
      axios.get(transactionsUrl)
        .then(function (response) {
          vm.items = response.data
        })
        .catch(function (error) {
          console.log('Error! Could not reach the API. ' + error)
        })

    },
    getAnswer: function () {

      var searchTerm = this.question
      if (this.question.length > 0) {
        searchTerm = searchTerm + '*'
      }

      var searchUrl = '/api/search?term=' + searchTerm
      var vm = this
      axios.get(searchUrl)
        .then(function (response) {
          vm.searchitems = response.data
        })
        .catch(function (error) {
          console.log('Error! Could not reach the API. ' + error)
        })
    }

  }
})