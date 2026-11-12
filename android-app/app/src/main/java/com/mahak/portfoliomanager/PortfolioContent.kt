package com.mahak.portfoliomanager

data class Project(
    val id: Int,
    val title: String,
    val description: String,
    val role: String,
    val technologies: List<String>,
    val teamSize: String
)

data class Experience(
    val id: Int,
    val position: String,
    val company: String,
    val location: String,
    val description: String
)

data class Service(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String
)

data class PortfolioContent(
    val projects: List<Project>,
    val experiences: List<Experience>,
    val services: List<Service>,
    val skills: List<String>
) {
    companion object {
        fun defaults() = PortfolioContent(
            projects = listOf(
                Project(1, "Game Onn", "A venue booking platform that allows players to book sports venues while enabling venue hosts to list, manage, and schedule availability.", "Full Stack Developer", listOf("NextJS", "Redux", "Regex", "MongoDB", "Tailwind CSS", "TypeScript"), "5–6"),
                Project(2, "Music Application", "A mobile app for streaming music, featuring categorized browsing, searching, audio playback, and a smooth cross-platform UI.", "Full Stack Developer", listOf("React Native", "NextJS", "Redux", "MongoDB", "Tailwind CSS", "TypeScript"), "10–12"),
                Project(3, "Apna Pump", "An on-demand fuel delivery app allowing drivers to complete and manage deliveries in real time via an intuitive mobile interface.", "Full Stack Developer", listOf("NextJS", "Redux", "Regex", "MongoDB", "Tailwind CSS", "TypeScript"), "10–12"),
                Project(4, "Matka App", "A mobile gaming app for number-based satta betting. Users can bet, check results, and manage their history with a secure interface.", "Full Stack Developer", listOf("React Native", "NextJS", "PostgreSQL", "Redux", "Tailwind CSS", "TypeScript"), "8–9"),
                Project(5, "Food Recipe App", "A cross-platform mobile app featuring detailed recipes, smart grocery list, cooking timer with notifications, nutrition info, multi-language support, subscriptions, and premium features.", "React Native Developer", listOf("React Native", "Expo", "Firebase", "Redux Toolkit", "AsyncStorage"), "SOLO"),
                Project(6, "Hindi Shayari App", "A mobile app offering a collection of categorized Hindi Shayari with options to mark favorites, share content, multi-language support, and a custom settings section.", "React Native Developer", listOf("React Native", "Expo", "AsyncStorage", "React Navigation"), "SOLO"),
                Project(7, "Education Management System", "A full-stack web application designed for managing student records, attendance, assignments, and exam results. Includes search, filter, sort, pagination, and CRUD functionality.", "Full Stack Developer", listOf("React", "Node.js", "Express.js", "MongoDB", "Axios"), "SOLO"),
                Project(8, "Chat Application", "A simple real-time chat application that allows users to send and receive messages with authentication and basic UI.", "Web Developer", listOf("React", "Node.js", "Socket.io", "MongoDB"), "SOLO")
            ),
            experiences = listOf(
                Experience(1, "Mobile Application Developer", "Staveo Tech Pvt. Ltd.", "New Delhi", "Working as a full-time Mobile App Developer, building high-quality Android and iOS applications using React Native and Expo. Responsible for implementing core features, integrating Firebase, managing state with Redux Toolkit, and optimizing performance. Also involved in publishing apps to the Play Store and maintaining production builds."),
                Experience(2, "Associate Software Development Engineer Intern", "Geeks2Connect Infotech Pvt Ltd", "Jaipur", "Joined as an ASDE Intern and contributed as a Full Stack Developer and App Developer. Worked on various web and mobile application projects using React, React Native, Expo, Express.js, MongoDB, and Firebase. Gained experience in individually handling projects, designing UI, managing APIs, integrating third-party services like payment gateways, and deploying apps."),
                Experience(3, "Node.js Developer Intern", "Celebal Technologies", "Jaipur", "Worked as a backend developer using Node.js and Express. Built REST APIs and collaborated with frontend teams to integrate features. Gained hands-on experience with MongoDB, authentication mechanisms, and project structure."),
                Experience(4, "MERN Stack Developer", "Learn and Build", "Remote", "Developed full-stack web applications using the MERN stack. Built interactive UIs with React, implemented APIs in Node.js/Express, and used MongoDB for data storage. Enhanced skills in deployment, project architecture, and version control.")
            ),
            services = listOf(
                Service(1, "JavaScript Development", "Creating dynamic and interactive web applications using JavaScript.", "⚙️"),
                Service(2, "React.js Development", "Building modern and responsive user interfaces with React.js.", "⚛️"),
                Service(3, "Node.js Backend", "Developing scalable server-side applications using Node.js.", "🚀"),
                Service(4, "Next.js Development", "Creating server-rendered React applications with Next.js.", "▲"),
                Service(5, "TypeScript Development", "Ensuring robust and maintainable code with TypeScript.", "📘"),
                Service(6, "Tailwind CSS Styling", "Designing beautiful and responsive interfaces with Tailwind CSS.", "🎨"),
                Service(7, "Mobile App Development", "Designing beautiful and responsive interfaces for mobile view with React Native.", "📱"),
                Service(8, "Backend Development", "Designing and implementing database schemas, queries, and integrations using MongoDB.", "🗄️")
            ),
            skills = listOf(
                "JavaScript", "TypeScript", "React.js", "Next.js", "Node.js", "Express.js",
                "React Native", "Expo", "Socket.io", "MongoDB", "PostgreSQL", "Firebase",
                "Redux", "Tailwind CSS", "HTML", "CSS"
            )
        )
    }
}
